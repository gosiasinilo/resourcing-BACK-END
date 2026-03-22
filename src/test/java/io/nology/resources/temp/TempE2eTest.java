package io.nology.resources.temp;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;

import io.nology.resources.E2eTestSuite;
import static io.restassured.RestAssured.given;

public class TempE2eTest extends E2eTestSuite {

        @Test
        void createTemp_shouldReturn201() {
                given()
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
                                .statusCode(201)
                                .body("id", notNullValue())
                                .body("firstName", equalTo("John"))
                                .body("lastName", equalTo("Brown"));
        }

        @Test
        void getAllTemps_shouldReturnList() {
                given()
                                .when()
                                .get("/temps")
                                .then()
                                .statusCode(200)
                                .body("size()", greaterThan(0));
        }

        @Test
        void getTempById_shouldReturnTempWithJobs() {

                int id = given()
                                .contentType("application/json")
                                .body("""
                                                    {
                                                      "firstName": "Anna",
                                                      "lastName": "Smith"
                                                    }
                                                """)
                                .when()
                                .post("/temps")
                                .then()
                                .extract()
                                .path("id");

                given()
                                .when()
                                .get("/temps/" + id)
                                .then()
                                .statusCode(200)
                                .body("id", equalTo(id))
                                .body("jobs", notNullValue());
        }

        @Test
        void getTemps_invalidDateRange_shouldReturn400() {
                given()
                                .when()
                                .get("/temps?startDate=2026-05-10&endDate=2026-05-01")
                                .then()
                                .statusCode(400)
                                .body("message", notNullValue());
        }

        @Test
        void getAvailableTempsByDate_shouldReturnList() {
                given()
                                .when()
                                .get("/temps?startDate=2026-06-01&endDate=2026-06-10")
                                .then()
                                .statusCode(200)
                                .body("$", notNullValue());
        }

        @Test
        void getAvailableTempsByJob_shouldReturnList() {

                int jobId = given()
                                .contentType("application/json")
                                .body("""
                                                    {
                                                      "name": "Warehouse inventory",
                                                      "startDate": "2026-08-01",
                                                      "endDate": "2026-08-05"
                                                    }
                                                """)
                                .when()
                                .post("/jobs")
                                .then()
                                .statusCode(201)
                                .extract()
                                .path("id");

                given()
                                .when()
                                .get("/temps?jobId=" + jobId)
                                .then()
                                .statusCode(200)
                                .body("$", notNullValue());
        }
}