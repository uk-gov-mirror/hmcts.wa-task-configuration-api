package uk.gov.hmcts.reform.wataskconfigurationapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.UserInfo;
import uk.gov.hmcts.reform.wataskconfigurationapi.utils.CreateTaskMessage;
import uk.gov.hmcts.reform.wataskconfigurationapi.utils.RoleAssignmentHelper;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.utils.CreateTaskMessageBuilder.createBasicMessageForTask;

@Slf4j
public class PostConfigureTaskTest extends SpringBootFunctionalBaseTest {

    private static final String ENDPOINT_BEING_TESTED = "task/{task-id}";
    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamSystemTokenGenerator systemTokenGenerator;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private RoleAssignmentHelper roleAssignmentHelper;

    private String taskId;
    private CreateTaskMessage createTaskMessage;
    private String caseId;

    @After
    public void cleanUp() {
        super.cleanUp(taskId);
    }

    @Test
    public void given_configure_task_then_expect_task_state_is_assigned() throws Exception {
        caseId = createCcdCase();
        createTaskMessage = createBasicMessageForTask()
            .withCaseId(caseId)
            .build();
        taskId = createTask(createTaskMessage);

        log.info("Creating roles");
        roleAssignmentHelper.setRoleAssignments(caseId);

        Response result = restApiActions.post(
            ENDPOINT_BEING_TESTED,
            taskId,
            new Headers(authorizationHeadersProvider.getServiceAuthorizationHeader())
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE);

        Response camundaResult = camundaApiActions.get(
            "/task/{task-id}/variables",
            taskId,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        camundaResult.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE)
            .body("caseName.value", is("Bob Smith"))
            .body("appealType.value", is("protection"))
            .body("region.value", is("1"))
            .body("location.value", is("765324"))
            .body("locationName.value", is("Taylor House"))
            .body("taskState.value", is("assigned"))
            .body("caseId.value", is(createTaskMessage.getCaseId()))
            .body("securityClassification.value", is("PUBLIC"))
            .body("caseTypeId.value", is("Asylum"))
            .body("title.value", is("task name"))
            .body("tribunal-caseworker.value", is("Read,Refer,Own,Manage,Cancel"))
            .body("senior-tribunal-caseworker.value", is("Read,Refer,Own,Manage,Cancel"));
    }

    @Test
    public void given_configure_task_then_expect_task_state_is_unassigned() throws IOException {

        caseId = createCcdCase();
        createTaskMessage = createBasicMessageForTask()
            .withCaseId(caseId)
            .build();
        taskId = createTask(createTaskMessage);

        Response result = restApiActions.post(
            ENDPOINT_BEING_TESTED,
            taskId,
            new Headers(authorizationHeadersProvider.getServiceAuthorizationHeader())
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE);

        Response camundaResult = camundaApiActions.get(
            "/task/{task-id}/variables",
            taskId,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        camundaResult.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE)
            .body("caseName.value", is("Bob Smith"))
            .body("appealType.value", is("protection"))
            .body("region.value", is("1"))
            .body("location.value", is("765324"))
            .body("locationName.value", is("Taylor House"))
            .body("taskState.value", is("unassigned"))
            .body("caseId.value", is(createTaskMessage.getCaseId()))
            .body("securityClassification.value", is("PUBLIC"))
            .body("caseTypeId.value", is("Asylum"))
            .body("title.value", is("task name"))
            .body("tribunal-caseworker.value", is("Read,Refer,Own,Manage,Cancel"))
            .body("senior-tribunal-caseworker.value", is("Read,Refer,Own,Manage,Cancel"));
    }

    private String createTask(CreateTaskMessage createTaskMessage) {

        Response camundaResult = camundaApiActions.post(
            "/message",
            createTaskMessage,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        camundaResult.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        Object taskName = createTaskMessage.getProcessVariables().get("name").getValue();

        String filter = "?processVariables=" + "caseId_eq_" + createTaskMessage.getCaseId();

        Response camundaGetTaskResult = camundaApiActions.get(
            "/task" + filter,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        return camundaGetTaskResult.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE)
            .body("size()", is(1))
            .body("[0].name", is(taskName))
            .extract()
            .path("[0].id");

    }

    private String createCcdCase() throws IOException {
        String userToken = systemTokenGenerator.generate();
        UserInfo userInfo = systemTokenGenerator.getUserInfo(userToken);
        String serviceToken = serviceAuthTokenGenerator.generate();
        StartEventResponse startCase = coreCaseDataApi.startForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            "startAppeal"
        );

        String caseData = new String(
            (Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("case_data.json"))).readAllBytes()
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

        log.info("Created case [" + caseDetails.getId() + "]");

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
        log.info("Submitted case [" + caseDetails.getId() + "]");

        return caseDetails.getId().toString();
    }

}
