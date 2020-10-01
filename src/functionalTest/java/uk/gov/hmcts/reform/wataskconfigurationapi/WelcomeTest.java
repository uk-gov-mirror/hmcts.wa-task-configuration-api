package uk.gov.hmcts.reform.wataskconfigurationapi;

import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;

public class WelcomeTest extends BaseFunctionalTest {

    @Test
    public void should_welcome_with_200_response_code() {

        SerenityRest.given()
            .when()
            .get("/")
            .then()
            .statusCode(HttpStatus.OK.value())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(containsString("Welcome to wa-task-configuration-api"));
    }
}
