package uk.gov.hmcts.reform.wataskconfigurationapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskRequest;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.CreateTaskMessageBuilder.createBasicMessageForTask;
import static uk.gov.hmcts.reform.wataskconfigurationapi.CreatorObjectMapper.asCamundaJsonString;
import static uk.gov.hmcts.reform.wataskconfigurationapi.CreatorObjectMapper.asJsonString;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ConfigureTaskTest {
    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final String camundaUrl =
        System.getenv("CAMUNDA_URL") == null ? "http://localhost:8080/engine-rest" : System.getenv("CAMUNDA_URL");
    private final String testUrl =
        System.getenv("TEST_URL") == null ? "http://localhost:8091" :  System.getenv("TEST_URL");

    @Test
    public void canConfigureATask() {
        CreateTaskMessage createTaskMessage = createBasicMessageForTask().build();
        String taskId = createTask(createTaskMessage);

        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(testUrl)
            .basePath("/configureTask")
            .body(asJsonString(new ConfigureTaskRequest(taskId)))
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.OK_200);

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/localVariables")
            .when()
            .get()
            .then()
            .body("appealType.value", is("PA/53816/2019"))
            .body("ccdId.value", is(createTaskMessage.getCcdId()))
        ;
    }

    private String createTask(CreateTaskMessage createTaskMessage) {
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/message")
            .body(asCamundaJsonString(createTaskMessage))
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);

        return given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task")
            .param("processVariables", "ccdId_eq_" + createTaskMessage.getCcdId())
            .when()
            .get()
            .then()
            .body("size()", is(1))
            .body("[0].name", is("Process Task"))
            .extract()
            .path("[0].id");
    }

}
