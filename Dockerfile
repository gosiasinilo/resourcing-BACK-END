# syntax=docker/dockerfile:1

# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Build the executable jar
COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

# spring-boot-maven-plugin repackages this into an executable fat jar
COPY --from=build /app/target/resources-0.0.1-SNAPSHOT.jar app.jar

# Container Apps sends traffic to this port
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
