package uk.gov.hmcts.reform.wataskconfigurationapi;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public abstract class BaseFunctionalTest {
    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Value("${targets.instance}")
    protected String testUrl;
    @Value("${targets.camunda}")
    protected String camundaUrl;

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    public void cleanUp(String taskId, String token) {
        given()
            .header(SERVICE_AUTHORIZATION, token)
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/complete")
            .when()
            .post();

        given()
            .header(SERVICE_AUTHORIZATION, token)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .when()
            .get("/history/task?taskId=" + taskId)
            .then()
            .body("[0].deleteReason", is("completed"));
    }

}
