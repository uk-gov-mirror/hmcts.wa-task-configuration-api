package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskconfigurationapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.request.ConfigureTaskRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.CreateTaskMessage;

import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.NAME;
import static uk.gov.hmcts.reform.wataskconfigurationapi.utils.CreateTaskMessageBuilder.createBasicMessageForTask;

@Slf4j
public class PostTaskConfigurationTest extends SpringBootFunctionalBaseTest {

    private static final String ENDPOINT_BEING_TESTED = "task/{task-id}/configuration";

    private String taskId;
    private String caseId;
    private CreateTaskMessage createTaskMessage;

    @After
    public void cleanUp() {
        super.cleanUp(taskId);
    }

    @Test
    public void should_return_task_configuration_then_expect_task_is_auto_assigned() throws Exception {
        caseId = createCcdCase();
        createTaskMessage = createBasicMessageForTask()
            .withCaseId(caseId)
            .build();
        taskId = createTask(createTaskMessage);
        log.info("task found [{}]", taskId);

        Map<String, Object> requiredProcessVariables = Map.of(
            CASE_ID.value(), caseId,
            NAME.value(), "task name"
        );

        log.info("Creating roles...");
        roleAssignmentHelper.setRoleAssignments(caseId);

        Response result = restApiActions.post(
            ENDPOINT_BEING_TESTED,
            taskId,
            new ConfigureTaskRequest(requiredProcessVariables),
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        result.prettyPeek();


        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE)
            .body("task_id", equalTo(taskId))
            .body("case_id", equalTo(caseId))
            .body("assignee", notNullValue())
            .body("configuration_variables", notNullValue())
            .body("configuration_variables.jurisdiction", equalTo("IA"))
            .body("configuration_variables.caseTypeId", equalTo("Asylum"))
            .body("configuration_variables.taskState", equalTo("assigned"))
            .body("configuration_variables.executionType", equalTo("Case Management Task"))
            .body("configuration_variables.caseId", equalTo(caseId))
            .body("configuration_variables.securityClassification", equalTo("PUBLIC"))
            .body("configuration_variables.autoAssigned", equalTo(true))
            .body("configuration_variables.taskSystem", equalTo("SELF"));
    }

    @Test
    public void should_return_task_configuration_then_expect_task_is_unassigned() throws Exception {
        caseId = createCcdCase();
        createTaskMessage = createBasicMessageForTask()
            .withCaseId(caseId)
            .build();
        taskId = createTask(createTaskMessage);
        log.info("task found [{}]", taskId);

        Map<String, Object> requiredProcessVariables = Map.of(
            CASE_ID.value(), caseId,
            NAME.value(), "task name"
        );

        Response result = restApiActions.post(
            ENDPOINT_BEING_TESTED,
            taskId,
            new ConfigureTaskRequest(requiredProcessVariables),
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(APPLICATION_JSON_VALUE)
            .body("task_id", equalTo(taskId))
            .body("case_id", equalTo(caseId))
            .body("assignee", nullValue())
            .body("configuration_variables", notNullValue())
            .body("configuration_variables.jurisdiction", equalTo("IA"))
            .body("configuration_variables.caseTypeId", equalTo("Asylum"))
            .body("configuration_variables.taskState", equalTo("unassigned"))
            .body("configuration_variables.executionType", equalTo("Case Management Task"))
            .body("configuration_variables.caseId", equalTo(caseId))
            .body("configuration_variables.securityClassification", equalTo("PUBLIC"))
            .body("configuration_variables.autoAssigned", equalTo(false))
            .body("configuration_variables.taskSystem", equalTo("SELF"));
    }

}
