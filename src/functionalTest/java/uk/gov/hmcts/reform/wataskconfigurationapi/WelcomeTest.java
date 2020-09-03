package uk.gov.hmcts.reform.wataskconfigurationapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
public class WelcomeTest {

    @Value("${targetInstance}") private String testUrl;
    @LocalServerPort
    private int port;

    @Before
    public void setUp() {

        if (testUrl.equals("http://localhost")) {
            RestAssured.port = port;
        } else {
            RestAssured.baseURI = testUrl;
        }
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_welcome_with_200_response_code() {

        Response response = SerenityRest.given()
            .when()
            .get("/");

        assertThat(response.statusCode())
            .isEqualTo(HttpStatus.OK.value());
        assertThat(response.contentType())
            .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.body().asString())
            .contains("Welcome to wa-workflow-api");

    }
}
