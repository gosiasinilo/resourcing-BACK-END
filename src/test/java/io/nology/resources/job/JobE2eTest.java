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
}