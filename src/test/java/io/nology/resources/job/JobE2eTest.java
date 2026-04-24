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
    LocalDate start2 = end.plusDays(1);
    LocalDate end2 = end.plusDays(5);
    LocalDate pastStart = LocalDate.now().minusDays(2);
    LocalDate pastEnd = LocalDate.now().plusDays(2);

    private int createTemp(String firstName, String email, String city) {
        return given()
                .contentType("application/json")
                .body("""
                            {
                              "firstName": "%s",
                              "lastName": "Test",
                              "email": "%s",
                              "city": "%s"
                            }
                        """.formatted(firstName, email, city))
                .when()
                .post("/temps")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private int createJob(String name, String jobType, String city) {
        String cityField = city != null
                ? "\"city\": \"%s\",".formatted(city)
                : "";

        return given()
                .contentType("application/json")
                .body("""
                            {
                              "name": "%s",
                              "jobType": "%s",
                              %s
                              "startDate": "%s",
                              "endDate": "%s"
                            }
                        """.formatted(name, jobType, cityField, start, end))
                .when()
                .post("/jobs")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void createJobWithLocation_shouldReturn201() {

        given()
                .contentType("application/json")
                .body("""
                            {
                              "name": "Signs installation",
                               "jobType": "LOCATION",
                              "city": "Melbourne",
                              "startDate": "%s",
                              "endDate": "%s"
                            }
                        """.formatted(start.toString(), end.toString()))
                .when()
                .post("/jobs")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Signs installation"))
                .body("jobType", equalTo("LOCATION"))
                .body("city", equalTo("Melbourne"));
    }

    @Test
    void createJob_Online_shouldReturn201() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "name": "Data entry project",
                              "jobType": "ONLINE",
                              "startDate": "%s",
                              "endDate": "%s"
                            }
                        """.formatted(start, end))
                .when()
                .post("/jobs")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("jobType", equalTo("ONLINE"))
                .body("city", nullValue());
    }

    @Test
    void createJob_notOnline_shouldReturn400_whenNoCity() {
        given()
                .contentType("application/json")
                .body("""
                            {
                              "name": "Warehouse shift",
                              "jobType": "LOCATION",
                              "startDate": "%s",
                              "endDate": "%s"
                            }
                        """.formatted(start, end))
                .when()
                .post("/jobs")
                .then()
                .statusCode(400)
                .body("details.city", notNullValue());
    }

    @Test
    void getAllJobs_shouldReturnJobs() {
        createJob("Seed job", "ONLINE", null);

        given()
                .when()
                .get("/jobs")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void assignTemp_shouldReturnUpdatedJob() {

        int tempId = createTemp("John", "john.assign@example.com", "Melbourne");
        int jobId = createJob("Factory shift", "LOCATION", "Melbourne");

        given()
                .when()
                .patch("/jobs/" + jobId + "/assign?tempId=" + tempId)
                .then()
                .statusCode(200)
                .body("temp.id", equalTo(tempId))
                .body("temp.firstName", equalTo("John"))
                .body("status", equalTo("ASSIGNED"));
    }

    @Test
    void unassignTemp_shouldRemoveTemp() {
        int tempId = createTemp("Jane", "jane.unassign@example.com", "Sydney");
        int jobId = createJob("Cleaning warehouse", "ONLINE", null);

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
                .body("temp", nullValue())
                .body("status", equalTo("INITIATED"));
    }

    @Test
    void assignTemp_whenBusy_shouldReturn400() {
        int tempId = createTemp("Busy", "busy.temp@example.com", "Brisbane");
        int job1 = createJob("Event setup", "ONLINE", null);
        int job2 = createJob("Event teardown", "ONLINE", null);

        given()
                .when()
                .patch("/jobs/" + job1 + "/assign?tempId=" + tempId)
                .then()
                .statusCode(200);

        given()
                .when()
                .patch("/jobs/" + job2 + "/assign?tempId=" + tempId)
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Test
    void deleteJob_shouldReturn204() {
        int jobId = createJob("Test job", "ONLINE", null);

        given()
                .when()
                .delete("/jobs/" + jobId)
                .then()
                .statusCode(204);
    }

    @Test
    void editJob_shouldUpdateNameOnly() {
        int jobId = createJob("Original Job", "ONLINE", null);

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
        int jobId = createJob("Date Test Job", "ONLINE", null);

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
        int tempId = createTemp("Conflict", "conflict@example.com", "Adelaide");
        int job1 = createJob("Job 1", "ONLINE", null);

        given()
                .patch("/jobs/" + job1 + "/assign?tempId=" + tempId)
                .then()
                .statusCode(200);

        int job2 = given()
                .contentType("application/json")
                .body("""
                            {
                              "name": "Job 2",
                              "jobType": "ONLINE",
                              "startDate": "%s",
                              "endDate": "%s"
                            }
                        """.formatted(start2, end2))
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

    @Test
    void completeJob_shouldReturn200_withReview() {
        int tempId = createTemp("Complete", "complete.temp@example.com", "Melbourne");

        int jobId = given()
                .contentType("application/json")
                .body("""
                            {
                              "name": "Completed Job",
                              "jobType": "ONLINE",
                              "startDate": "%s",
                              "endDate": "%s"
                            }
                        """.formatted(pastStart, pastEnd))
                .when()
                .post("/jobs")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .when()
                .patch("/jobs/" + jobId + "/assign?tempId=" + tempId)
                .then()
                .statusCode(200);

        given()
                .contentType("application/json")
                .body("""
                            {
                              "workQuality": 5,
                              "communication": 4,
                              "onTime": 3,
                              "comments": "Great work"
                            }
                        """)
                .when()
                .post("/jobs/" + jobId + "/complete")
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"));
    }

    @Test
    void completeJob_shouldReturn400_whenJobNotStarted() {
        int tempId = createTemp("NotStarted", "notstarted@example.com", "Perth");
        int jobId = createJob("Future Job", "ONLINE", null);

        given()
                .when()
                .patch("/jobs/" + jobId + "/assign?tempId=" + tempId)
                .then()
                .statusCode(200);

        given()
                .contentType("application/json")
                .body("""
                            {
                              "workQuality": 5,
                              "communication": 4,
                              "onTime": 3
                            }
                        """)
                .when()
                .post("/jobs/" + jobId + "/complete")
                .then()
                .statusCode(400)
                .body("details.startDate", notNullValue());
    }

}