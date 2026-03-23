# Resourcing API (Backend)

## Demo & Snippets

- Backend API only
- Test using postman/swagger/curl
- Frontend client in progress

---

## Requirements / Purpose

- This project is build with Spring Boot. It is a simple back end app to assing jobs to contractors. It allows:
- Show all jobs (filtered by assigned/unassigned)
- Show job by Id
- Show all temps/contractors
- Show temp by Id with jobs assigned to them
- Create job
- Create temp
- Unassign/assign temp to the job - if temp busy show next available date and list of temps available on the date

# Assumptions

- Temp can not have more than 1 job assigned at the time
- Job can have only one temp assigned
- Temps are on contract - jobs lenght is in days inclusive of weekends and public holidays

# Stack

- Java + Spring Boot - bakend testing
- MySQL - relational database
- Faker - dev/test data seeding

---

## Build Steps

- Create .env file based on .env.example
- Install: mvn clean install
- Run the app: mvn spring-boot:run
- Run tests: mvn test

---

## Design Goals / Approach

- Clean structure with no repetitive code
- Scalable structure allowing adding more features
- Strong validation and error handling

---

## Features

- Create jobs
- Create temps
- Assign temp to job
- Unassign temp from job
- Prevent overlapping job assignments.
  If temp busy suggest:
  - next available date
  - list of alternative temps
- Get temp by Id with assigned jobs
- Get available temps:
  - by job
  - by date range
- Dev/test data seeding using Faker
- Global exception handling
- End-to-end tests

---

## Known issues

- No pagination
- Basic auth for 1 user only

---

## Future Goals

- Pagination and sorting
- Authentication
- Extra features : wage per day, budget per job etc.

---

## 23.03.2026

- 23.03.2026 Added first version on backend application

---

## What did I struggle with?

- E2E test failures (200 vs 201)

---

## Licensing Details

- What type of license are you releasing this under?

---

## Further details, related projects, reimplementations

- This project is similat to customer project we were working on on the end of Nology bootcamp
- It is a part of full stack project - Front End in progress
