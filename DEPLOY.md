# Deploying the Resourcing backend (Azure, from zero)

Target architecture:

| Piece    | Service                                   | Repo                         |
|----------|------------------------------------------|------------------------------|
| Frontend | Azure Static Web Apps (Free)             | `resources-frontend`         |
| Backend  | Azure Container Apps (this repo)         | `resourcing-BACK-END`        |
| Database | Azure Database for MySQL Flexible Server | –                            |
| Domain   | `resources.gosiasinilo.com` → SWA        | –                            |

The backend image is built by GitHub Actions, pushed to GHCR, and rolled out to
the Container App with `az containerapp update`. See
`.github/workflows/deploy-backend.yml`.

---

## 1. One-time Azure setup

Run these once from a machine with the Azure CLI logged into your **student**
subscription (`az login`, then `az account set --subscription "<name>"`).

```bash
# --- variables -------------------------------------------------------------
RG=resourcing-rg
LOCATION=australiaeast              # a region your student sub allows
ENV=resourcing-env                 # Container Apps environment
APP=resourcing-api                 # Container App name
MYSQL=resourcing-db-$RANDOM        # must be globally unique
DB_ADMIN=appadmin
DB_PASS='<choose-a-strong-password>'
IMAGE=ghcr.io/gosiasinilo/resourcing-back-end:latest   # lowercase!

# --- resource group ------------------------------------------------------
az group create --name $RG --location $LOCATION

# --- MySQL Flexible Server (Burstable B1ms) -----------------------------
az mysql flexible-server create \
  --resource-group $RG --name $MYSQL --location $LOCATION \
  --admin-user $DB_ADMIN --admin-password "$DB_PASS" \
  --sku-name Standard_B1ms --tier Burstable \
  --storage-size 20 --version 8.0.21 \
  --public-access 0.0.0.0                 # allow Azure services; tighten later

az mysql flexible-server db create \
  --resource-group $RG --server-name $MYSQL --database-name resources

# --- Container Apps environment + app ----------------------------------
az extension add --name containerapp --upgrade
az provider register --namespace Microsoft.App --wait
az provider register --namespace Microsoft.OperationalInsights --wait

az containerapp env create \
  --resource-group $RG --name $ENV --location $LOCATION

az containerapp create \
  --resource-group $RG --name $APP --environment $ENV \
  --image $IMAGE \
  --target-port 8080 --ingress external \
  --min-replicas 0 --max-replicas 2 \
  --cpu 0.5 --memory 1.0Gi \
  --secrets "db-pass=$DB_PASS" \
            "admin-pass=<frontend-and-api-shared-password>" \
            "anthropic-key=<your-anthropic-api-key>" \
  --env-vars \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_DATASOURCE_URL="jdbc:mysql://$MYSQL.mysql.database.azure.com:3306/resources?sslMode=REQUIRED&serverTimezone=UTC" \
    SPRING_DATASOURCE_USERNAME=$DB_ADMIN \
    SPRING_DATASOURCE_PASSWORD=secretref:db-pass \
    ADMIN_USERNAME=admin \
    ADMIN_PASSWORD=secretref:admin-pass \
    ANTHROPIC_API_KEY=secretref:anthropic-key \
    APP_CORS_ALLOWED_ORIGINS="https://resources.gosiasinilo.com,http://localhost:5173" \
    APP_SEED_DEMO_DATA=true          # first boot only – see step 4
```

`--min-replicas 0` = scales to zero when idle (near-zero credit burn, ~10–20 s
cold start on the first request).

Grab the backend URL:

```bash
az containerapp show -g $RG -n $APP --query properties.configuration.ingress.fqdn -o tsv
# e.g. resourcing-api.<hash>.australiaeast.azurecontainerapps.io
```

## 2. GitHub configuration (this repo)

**Make the GHCR image public** (simplest, no pull secret needed):
after the first workflow run, GitHub → repo → *Packages* → the package →
*Package settings* → *Change visibility* → Public. Otherwise configure a
registry PAT on the Container App.

**Create a deploy service principal:**

```bash
az ad sp create-for-rbac --name "resourcing-deploy" --role contributor \
  --scopes /subscriptions/<sub-id>/resourceGroups/$RG --sdk-auth
```

Copy the JSON output.

**Repo → Settings → Secrets and variables → Actions:**

| Kind     | Name                 | Value                                  |
|----------|----------------------|----------------------------------------|
| Secret   | `AZURE_CREDENTIALS`  | the `--sdk-auth` JSON above             |
| Variable | `ACA_APP_NAME`       | `resourcing-api`                       |
| Variable | `ACA_RESOURCE_GROUP` | `resourcing-rg`                        |

Push to `main` → the workflow builds, pushes, and rolls out.

## 3. Point the frontend at the backend

In the **frontend** repo → Settings → Secrets and variables → Actions → *Variables*:

```
VITE_API_URL = https://<backend-fqdn-from-step-1>
```

Redeploy the frontend (push any commit, or re-run its workflow). The SPA now
calls the Container App over HTTPS; CORS already allows
`https://resources.gosiasinilo.com` via `APP_CORS_ALLOWED_ORIGINS`.

## 4. Seed the demo data once

`APP_SEED_DEMO_DATA=true` (set in step 1) makes the **first** boot populate an
empty DB. It never deletes rows and skips if any data exists. After the first
successful deploy:

```bash
az containerapp update -g resourcing-rg -n resourcing-api \
  --remove-env-vars APP_SEED_DEMO_DATA
```

To reseed later: drop the tables (or the `resources` DB), set the var back to
`true`, restart, then remove it again.

## 5. Custom domain

Frontend stays the public entry point on `resources.gosiasinilo.com`
(Static Web Apps, free managed cert):

1. DNS host for `gosiasinilo.com`: add `CNAME  resources  →  <swa-default-hostname>.azurestaticapps.net`
   (find the hostname in the SWA *Overview* blade). On Cloudflare set it to
   "DNS only" until validated.
2. SWA → *Custom domains* → *+ Add* → *Custom domain on other DNS* →
   `resources.gosiasinilo.com` → validate via CNAME.

Optionally give the backend its own hostname (`api.resources.gosiasinilo.com`)
via `az containerapp hostname add` + a managed cert, and update `VITE_API_URL`
to match.

---

## Notes / caveats

- **Student subscription has a hard $0 cap.** When the $100 credit runs out,
  resources are *disabled* (not billed). MySQL Flexible B1ms is ~USD 13/mo of
  credit; the Container App is near-zero when idle.
- **The admin password is compiled into the frontend bundle** (`VITE_ADMIN_*`).
  Anyone can read it in devtools and call the API directly. Fine for a demo with
  a throwaway credential; not real auth.
- `spring.profiles.active` defaults to `dev` in `application.properties`; prod is
  selected purely by the `SPRING_PROFILES_ACTIVE=prod` env var.
- `application-prod.properties` sets `ddl-auto=update`, so Hibernate creates
  missing tables/columns but never drops. For anything beyond a demo, switch to
  Flyway migrations.
