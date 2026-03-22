package io.nology.resources.job;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;

import io.nology.resources.E2eTestSuite;
import static io.restassured.RestAssured.given;

public class JobE2eTest extends E2eTestSuite {

  @Test
  void createJob_shouldReturn201() {

    given()
        .contentType("application/json")
        .body("""
                {
                  "name": "Signs installation",
                  "startDate": "2026-04-01",
                  "endDate": "2026-04-10"
                }
            """)
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
                  "lastName": "Brown"
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
                  "startDate": "2026-05-01",
                  "endDate": "2026-05-05"
                }
            """)
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
                  "lastName": "Brown"
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
                  "startDate": "2026-06-01",
                  "endDate": "2026-06-05"
                }
            """)
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
                  "lastName": "Smith"
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
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-05"
                }
            """)
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
                  "startDate": "2026-07-02",
                  "endDate": "2026-07-04"
                }
            """)
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
}