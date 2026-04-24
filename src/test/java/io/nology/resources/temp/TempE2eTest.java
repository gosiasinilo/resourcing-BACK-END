package io.nology.resources.temp;

import static org.hamcrest.Matchers.equalTo;
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
                  "lastName": "Brown",
                  "email": "john.brown@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("firstName", equalTo("John"))
        .body("lastName", equalTo("Brown"))
        .body("email", equalTo("john.brown@example.com"))
        .body("city", equalTo("Melbourne"));
  }

  @Test
  void getTempById_shouldReturnTempWithJobs() {

    int id = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "Anna",
                  "lastName": "Smith",
                  "email": "anna.smith@example.com",
                  "city": "Melbourne"
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
        .body("jobs", notNullValue())
        .body("city", equalTo("Melbourne"));
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
  void createTemp_shouldReturn400_whenCityNotFound() {
    given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "John",
                  "lastName": "Brown",
                  "email": "john.brown@example.com",
                  "city": "not-a-city"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(400)
        .body("details.city", notNullValue());
  }

  @Test
  void deleteTemp_shouldReturn204() {

    int tempId = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .extract()
        .path("id");

    given()
        .when()
        .delete("/temps/" + tempId)
        .then()
        .statusCode(204);
  }

  @Test
  void editTemp_shouldUpdateFirstName() {
    int id = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "Alice",
                  "lastName": "Jones",
                  "email": "alice.jones@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "firstName": "Alicia" }
            """)
        .when()
        .patch("/temps/" + id)
        .then()
        .statusCode(200)
        .body("firstName", equalTo("Alicia"))
        .body("lastName", equalTo("Jones"))
        .body("email", equalTo("alice.jones@example.com"))
        .body("city", equalTo("Melbourne"));
  }

  @Test
  void editTemp_shouldUpdateEmail() {
    int id = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "Bob",
                  "lastName": "Smith",
                  "email": "bob.smith@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "email": "bob.new@example.com" }
            """)
        .when()
        .patch("/temps/" + id)
        .then()
        .statusCode(200)
        .body("email", equalTo("bob.new@example.com"))
        .body("firstName", equalTo("Bob"));
  }

  @Test
  void editTemp_shouldUpdateCity_andGiveCoordinates() {
    int id = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "Bob",
                  "lastName": "Smith",
                  "email": "bob.smith@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "city": "Sydney" }
            """)
        .when()
        .patch("/temps/" + id)
        .then()
        .statusCode(200)
        .body("city", equalTo("Sydney"));
  }

  @Test
  void editTemp_shouldReturn400_whenFirstNameTooShort() {
    int id = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "Carol",
                  "lastName": "White",
                  "email": "carol.white@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "firstName": "Jo" }
            """)
        .when()
        .patch("/temps/" + id)
        .then()
        .statusCode(400)
        .body("details.firstName", notNullValue());
  }

  @Test
  void editTemp_shouldReturn400_whenEmailInvalid() {
    int id = given()
        .contentType("application/json")
        .body("""
                {
                  "firstName": "Dave",
                  "lastName": "Black",
                  "email": "dave.black@example.com",
                  "city": "Melbourne"
                }
            """)
        .when()
        .post("/temps")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    given()
        .contentType("application/json")
        .body("""
                { "email": "not-an-email" }
            """)
        .when()
        .patch("/temps/" + id)
        .then()
        .statusCode(400)
        .body("details.email", notNullValue());
  }

  @Test
  void editTemp_shouldReturn404_whenTempNotFound() {
    given()
        .contentType("application/json")
        .body("""
                { "firstName": "Ghost" }
            """)
        .when()
        .patch("/temps/99999")
        .then()
        .statusCode(404);
  }
}