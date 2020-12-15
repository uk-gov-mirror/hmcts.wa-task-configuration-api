package uk.gov.hmcts.reform.wataskconfigurationapi;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.wataskconfigurationapi.config.RestApiActions;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.AuthorizationHeadersProvider;

import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public abstract class SpringBootFunctionalBaseTest {
    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final String ENDPOINT_COMPLETE_TASK = "task/{task-id}/complete";
    private static final String ENDPOINT_HISTORY_TASK = "history/task";

    @Value("${targets.instance}")
    protected String testUrl;
    @Value("${targets.camunda}")
    protected String camundaUrl;

    protected RestApiActions restApiActions;
    protected RestApiActions camundaApiActions;

    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Before
    public void setUpGivens() {

        restApiActions = new RestApiActions(testUrl, SNAKE_CASE).setUp();
        camundaApiActions = new RestApiActions(camundaUrl, LOWER_CAMEL_CASE).setUp();

    }

    public void cleanUp(String taskId) {

        camundaApiActions.post(
            ENDPOINT_COMPLETE_TASK,
            taskId,
            new Headers(authorizationHeadersProvider.getServiceAuthorizationHeader()));

        Response result = camundaApiActions.get(
            ENDPOINT_HISTORY_TASK + "?taskId=" + taskId,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("[0].deleteReason", is("completed"));

    }

}
