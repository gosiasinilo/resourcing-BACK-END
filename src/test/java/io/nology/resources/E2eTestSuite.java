package io.nology.resources;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.nology.resources.common.services.LocationService;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class E2eTestSuite {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private LocationService locationService;

    private final String USER = "admin";
    private final String PASS = "Password123";

    @BeforeAll
    public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.requestSpecification = RestAssured.given().auth().preemptive().basic(USER, PASS);

        Mockito.when(locationService.getCoordinates(Mockito.anyString()))
                .thenReturn(Optional.of(new double[] { -37.8136, 144.9631 }));

        Mockito.when(locationService.getCoordinates("NotARealCityXYZ123"))
                .thenReturn(Optional.empty());
    }

    @BeforeEach
    public void resetDatabase() {
        jdbcTemplate.execute("DELETE FROM job_required_skills");
        jdbcTemplate.execute("DELETE FROM temp_skills");
        jdbcTemplate.execute("DELETE FROM job_review");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM temps");
        jdbcTemplate.execute("DELETE FROM skills");
        Mockito.when(locationService.getCoordinates(Mockito.anyString()))
                .thenReturn(Optional.of(new double[] { -37.8136, 144.9631 }));

        Mockito.when(locationService.getCoordinates("not-a-city"))
                .thenReturn(Optional.empty());
    }
}