package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskconfigurationapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.CreateTaskMessage;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.utils.CreateTaskMessageBuilder.createBasicMessageForTask;

@Slf4j
public class PostConfigureTaskTest extends SpringBootFunctionalBaseTest {

    private static final String ENDPOINT_BEING_TESTED = "task/{task-id}";

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
        log.info("task found [{}]", taskId);

        log.info("Creating roles...");
        roleAssignmentHelper.setRoleAssignments(caseId);

        Response result = restApiActions.post(
            ENDPOINT_BEING_TESTED,
            taskId,
            new Headers(authorizationHeadersProvider.getServiceAuthorizationHeader())
        );
        result.prettyPeek();

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE);

        Response camundaResult = camundaApiActions.get(
            "/task/{task-id}/variables",
            taskId,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        camundaResult.prettyPeek();

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
            .body("jurisdiction.value", is("IA"))
            .body("caseTypeId.value", is("Asylum"))
            .body("title.value", is("task name"))
            .body("hasWarnings.value", is("false"))
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
        log.info("task found [{}]", taskId);

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
            .body("jurisdiction.value", is("IA"))
            .body("caseTypeId.value", is("Asylum"))
            .body("title.value", is("task name"))
            .body("tribunal-caseworker.value", is("Read,Refer,Own,Manage,Cancel"))
            .body("senior-tribunal-caseworker.value", is("Read,Refer,Own,Manage,Cancel"));
    }

}
