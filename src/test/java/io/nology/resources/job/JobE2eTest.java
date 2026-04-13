package io.nology.resources.job;

import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;

import io.nology.resources.E2eTestSuite;
import static io.restassured.RestAssured.given;

public class JobE2eTest extends E2eTestSuite {
  LocalDate start = LocalDate.now().plusDays(1);
  LocalDate end = LocalDate.now().plusDays(5);

  @Test
  void createJob_shouldReturn201() {

    given()
        .contentType("application/json")
        .body("""
                {
                  "name": "Signs installation",
                  "startDate": "%s",
                  "endDate": "%s"
                }
            """.formatted(start.toString(), end.toString()))
        .when()
        .post("/jobs")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("Signs installation"));
  }

  @Test
  void getAllJobs_shouldReturnJobs() {
    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0));
  }

  @Test
  void assignTemp_shouldReturnUpdatedJob() {

    int tempId = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "John",
                  "lastName": "Brown",
                  "email": "john.brown@example.com"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .extract()
        .path("id");

    int jobId = given()
        .contentType("application/json")
        .body("""
                {
                  "name": "Factory shift",
                   "startDate": "%s",
                  "endDate": "%s"
                }
            """.formatted(start.toString(), end.toString()))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .when()
        .patch("/jobs/" + jobId + "/assign?tempId=" + tempId)
        .then()
        .statusCode(200)
        .body("temp.id", equalTo(tempId));
  }

  @Test
  void unassignTemp_shouldRemoveTemp() {

    int tempId = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "John",
                  "lastName": "Brown",
                  "email": "john.brown@example.com"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .extract()
        .path("id");

    int jobId = given()
        .contentType("application/json")
        .body("""
                {
                  "name": "Cleaning warehouse",
                   "startDate": "%s",
                  "endDate": "%s"
                }
            """.formatted(start.toString(), end.toString()))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .when()
        .patch("/jobs/" + jobId + "/assign?tempId=" + tempId)
        .then()
        .statusCode(200);

    given()
        .when()
        .patch("/jobs/" + jobId + "/unassign")
        .then()
        .statusCode(200)
        .body("temp", nullValue());
  }

  @Test
  void assignTemp_whenBusy_shouldReturn400() {

    int tempId = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "John",
                  "lastName": "Smith",
                  "email": "john.smith@example.com"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .extract()
        .path("id");

    int job1 = given()
        .contentType("application/json")
        .body("""
                {
                  "name": "Event setup",
                   "startDate": "%s",
                  "endDate": "%s"
                }
            """.formatted(start.toString(), end.toString()))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .when()
        .patch("/jobs/" + job1 + "/assign?tempId=" + tempId)
        .then()
        .statusCode(200);

    int job2 = given()
        .contentType("application/json")
        .body("""
                {
                  "name": "Event teardown",
                  "startDate": "%s",
                  "endDate": "%s"
                }
            """.formatted(start.toString(), end.toString()))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .when()
        .patch("/jobs/" + job2 + "/assign?tempId=" + tempId)
        .then()
        .statusCode(400)
        .body("message", notNullValue());
  }

  @Test
  void deleteJob_shouldReturn204() {

    int jobId = given()
        .contentType("application/json")
        .body("""
            {
              "name": "Test job",
             "startDate": "%s",
                          "endDate": "%s"
                        }
                    """.formatted(start.toString(), end.toString()))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .when()
        .delete("/jobs/" + jobId)
        .then()
        .statusCode(204);
  }

  @Test
  void editJob_shouldUpdateNameOnly() {
    int jobId = given()
        .contentType("application/json")
        .body("""
                { "name": "Original Job", "startDate": "%s", "endDate": "%s" }
            """.formatted(start, end))
        .when()
        .post("/jobs")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "name": "New Job Name" }
            """)
        .when()
        .patch("/jobs/" + jobId)
        .then()
        .statusCode(200)
        .body("name", equalTo("New Job Name"))
        .body("startDate", notNullValue())
        .body("endDate", notNullValue());
  }

  @Test
  void editJob_shouldReturn400_whenEndDateBeforeStartDate() {
    int jobId = given()
        .contentType("application/json")
        .body("""
                { "name": "Date Test Job", "startDate": "%s", "endDate": "%s" }
            """.formatted(start, end))
        .when()
        .post("/jobs")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "startDate": "%s", "endDate": "%s" }
            """.formatted(end, start))
        .when()
        .patch("/jobs/" + jobId)
        .then()
        .statusCode(400)
        .body("details.endDate", notNullValue());
  }

  @Test
  void editJob_shouldReturn404_whenJobNotFound() {
    given()
        .contentType("application/json")
        .body("""
                { "name": "Ghost Job" }
            """)
        .when()
        .patch("/jobs/99999")
        .then()
        .statusCode(404);
  }

  @Test
  void editJob_shouldFail_whenTempUnavailableForNewDates() {
    int tempId = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "John",
                  "lastName": "Smith",
                  "email": "john.smith@example.com"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .extract()
        .path("id");

    int job1 = given()
        .contentType("application/json")
        .body("""
                { "name": "Job 1", "startDate": "%s", "endDate": "%s" }
            """.formatted(start, end))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .patch("/jobs/" + job1 + "/assign?tempId=" + tempId)
        .then()
        .statusCode(200);

    LocalDate job2Start = end.plusDays(1);
    LocalDate job2End = end.plusDays(5);

    int job2 = given()
        .contentType("application/json")
        .body("""
                { "name": "Job 2", "startDate": "%s", "endDate": "%s" }
            """.formatted(job2Start, job2End))
        .when()
        .post("/jobs")
        .then()
        .extract()
        .path("id");

    given()
        .patch("/jobs/" + job2 + "/assign?tempId=" + tempId)
        .then()
        .statusCode(200);

    given()
        .contentType("application/json")
        .body("""
                { "startDate": "%s", "endDate": "%s" }
            """.formatted(start, end))
        .when()
        .patch("/jobs/" + job2)
        .then()
        .statusCode(400)
        .body("errorCode", equalTo("BAD_REQUEST"))
        .body("details.temp", notNullValue());
  }
}