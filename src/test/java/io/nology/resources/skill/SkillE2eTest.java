package io.nology.resources.skill;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;

import io.nology.resources.E2eTestSuite;
import static io.restassured.RestAssured.given;

public class SkillE2eTest extends E2eTestSuite {

    @Test
    void createSkill_shouldReturn201() {
        given()
                .contentType("application/json")
                .body("""
                            { "name": "Forklift" }
                        """)
                .when()
                .post("/skills")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Forklift"));
    }

    @Test
    void getAllSkills_shouldReturnList() {
        given()
                .contentType("application/json")
                .body("""
                            { "name": "Driving" }
                        """)
                .when()
                .post("/skills");

        given()
                .when()
                .get("/skills")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void createSkill_shouldReturn400_whenNameBlank() {
        given()
                .contentType("application/json")
                .body("""
                            { "name": "" }
                        """)
                .when()
                .post("/skills")
                .then()
                .statusCode(400);
    }

    @Test
    void deleteSkill_shouldReturn204() {
        int id = given()
                .contentType("application/json")
                .body("""
                            { "name": "Packing" }
                        """)
                .when()
                .post("/skills")
                .then()
                .extract()
                .path("id");

        given()
                .when()
                .delete("/skills/" + id)
                .then()
                .statusCode(204);
    }
}