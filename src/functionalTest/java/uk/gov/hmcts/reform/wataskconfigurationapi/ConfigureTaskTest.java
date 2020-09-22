package uk.gov.hmcts.reform.wataskconfigurationapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.idam.UserInfo;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.CreateTaskMessageBuilder.createBasicMessageForTask;
import static uk.gov.hmcts.reform.wataskconfigurationapi.CreatorObjectMapper.asCamundaJsonString;
import static uk.gov.hmcts.reform.wataskconfigurationapi.CreatorObjectMapper.asJsonString;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class ConfigureTaskTest {
    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final String camundaUrl =
        System.getenv("CAMUNDA_URL") == null ? "http://localhost:8080/engine-rest" : System.getenv("CAMUNDA_URL");
    private final String testUrl =
        System.getenv("TEST_URL") == null ? "http://localhost:8091" :  System.getenv("TEST_URL");

    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private IdamSystemTokenGenerator systemTokenGenerator;
    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    public void canConfigureATask() throws IOException {
        String ccdId = createCcdCase();

        CreateTaskMessage createTaskMessage = createBasicMessageForTask()
            .withCcdId(ccdId)
            .build();

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
            //.body("appealType.value", is("RP/50001/2020"))
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

    private String createCcdCase() throws IOException {
        String userToken = "Bearer " + systemTokenGenerator.generate();
        UserInfo userInfo = systemTokenGenerator.getUserInfo(userToken);
        String serviceToken = authTokenGenerator.generate();
        StartEventResponse startCase = coreCaseDataApi.startForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            "startAppeal"
        );
        String caseData = new String(
            (Thread.currentThread().getContextClassLoader().getResourceAsStream("case_data.json")).readAllBytes()
        );
        Map data = new ObjectMapper().readValue(caseData, Map.class);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startCase.getToken())
            .event(Event.builder()
                       .id(startCase.getEventId())
                       .summary("summary")
                       .description("description")
                       .build())
            .data(data)
            .build();
        CaseDetails caseDetails = coreCaseDataApi.submitForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            true,
            caseDataContent
        );

        System.out.println("Created case [" + caseDetails.getId() + "]");

        StartEventResponse submitCase = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            caseDetails.getId().toString(),
            "submitAppeal"
        );
        CaseDataContent submitCaseDataContent = CaseDataContent.builder()
            .eventToken(submitCase.getToken())
            .event(Event.builder()
                       .id(submitCase.getEventId())
                       .summary("summary")
                       .description("description")
                       .build())
            .data(data)
            .build();
        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            caseDetails.getId().toString(),
            true,
            submitCaseDataContent
        );

        return caseDetails.getId().toString();
    }

}
