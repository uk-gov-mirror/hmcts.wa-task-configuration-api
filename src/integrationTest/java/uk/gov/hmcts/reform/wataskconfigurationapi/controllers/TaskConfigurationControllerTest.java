package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.Token;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.ActorIdType;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.Classification;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleAssignment;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleAssignmentResource;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleCategory;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleName;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleType;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CcdDataServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.IdamServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.request.ConfigureTaskRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AssigneeRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DmnRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskconfigurationapi.controllers.util.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.stringValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.NAME;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.ASSIGNED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.UNASSIGNED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.UNCONFIGURED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.DmnEvaluationService.WA_TASK_CONFIGURATION_DECISION_TABLE_NAME;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TaskConfigurationControllerTest {

    private static final String TASK_NAME = "taskName";
    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";
    private static final String BEARER_USER_TOKEN = "Bearer user token";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CamundaServiceApi camundaServiceApi;
    @MockBean
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @MockBean
    private IdamSystemTokenGenerator systemTokenGenerator;
    @MockBean
    private CcdDataServiceApi ccdDataServiceApi;
    @MockBean
    private IdamServiceApi idamServiceApi;
    @MockBean
    private RoleAssignmentServiceApi roleAssignmentServiceApi;

    private String testTaskId;
    private String testProcessInstanceId;
    private String testUserId;
    private String testCaseId;

    @BeforeEach
    void setUp() {
        testTaskId = UUID.randomUUID().toString();
        testProcessInstanceId = UUID.randomUUID().toString();
        testUserId = UUID.randomUUID().toString();
        testCaseId = UUID.randomUUID().toString();
    }

    @DisplayName("Should return 200 and configure a task over REST with no auto-assign")
    @Test
    void should_succeed_and_configure_a_task_over_rest_with_no_auto_assignment() throws Exception {

        setupRoleAssignmentResponse(false);
        HashMap<String, CamundaValue<String>> modifications = configure3rdPartyResponses();

        mockMvc.perform(
            post("/task/" + testTaskId)
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();


        ArgumentCaptor<AddLocalVariableRequest> argumentCaptor = ArgumentCaptor.forClass(AddLocalVariableRequest.class);
        verify(camundaServiceApi, times(2)).addLocalVariablesToTask(
            eq(BEARER_SERVICE_TOKEN),
            eq(testTaskId),
            argumentCaptor.capture()
        );

        Map<String, CamundaValue<String>> stateUpdate = Map.of(TASK_STATE.value(), stringValue(UNASSIGNED.value()));

        List<AddLocalVariableRequest> capturedArguments = argumentCaptor.getAllValues();
        assertEquals(new AddLocalVariableRequest(modifications), capturedArguments.get(0));
        assertEquals(new AddLocalVariableRequest(stateUpdate), capturedArguments.get(1));


        verify(camundaServiceApi, never()).assignTask(
            eq(BEARER_SERVICE_TOKEN),
            eq(testTaskId),
            any()
        );
    }


    @DisplayName("Should return 200 and configure a task over REST with auto-assign")
    @Test
    void should_succeed_and_configure_a_task_over_rest_with_auto_assignment() throws Exception {

        setupRoleAssignmentResponse(true);
        HashMap<String, CamundaValue<String>> modifications = configure3rdPartyResponses();

        mockMvc.perform(
            post("/task/" + testTaskId)
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andReturn();


        ArgumentCaptor<AddLocalVariableRequest> argumentCaptor = ArgumentCaptor.forClass(AddLocalVariableRequest.class);
        verify(camundaServiceApi, times(2)).addLocalVariablesToTask(
            eq(BEARER_SERVICE_TOKEN),
            eq(testTaskId),
            argumentCaptor.capture()
        );

        Map<String, CamundaValue<String>> stateUpdate = Map.of(TASK_STATE.value(), stringValue(ASSIGNED.value()));

        List<AddLocalVariableRequest> capturedArguments = argumentCaptor.getAllValues();
        assertEquals(new AddLocalVariableRequest(modifications), capturedArguments.get(0));
        assertEquals(new AddLocalVariableRequest(stateUpdate), capturedArguments.get(1));


        verify(camundaServiceApi, times(1)).assignTask(
            BEARER_SERVICE_TOKEN,
            testTaskId,
            new AssigneeRequest(testUserId)
        );
    }

    @DisplayName("Should return 404 if task did not exist when configuring a task")
    @Test
    void should_fail_and_return_404_when_configuring_a_task_over_rest() throws Exception {

        when(camundaServiceApi.getTask(BEARER_SERVICE_TOKEN, testTaskId))
            .thenThrow(mock(FeignException.NotFound.class));
        when(systemTokenGenerator.generate()).thenReturn(BEARER_USER_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        mockMvc.perform(
            post("/task/" + testTaskId)
                .contentType(APPLICATION_JSON_VALUE)
        )
            .andExpect(status().isNotFound())
            .andReturn();

        verify(camundaServiceApi, never()).addLocalVariablesToTask(
            eq(BEARER_SERVICE_TOKEN),
            any(String.class),
            any(AddLocalVariableRequest.class)
        );
    }

    @DisplayName("Should return 200 and return configuration as body with auto-assignment")
    @Test
    void should_succeed_and_return_configuration_with_auto_assignment() throws Exception {

        setupRoleAssignmentResponse(true);
        configure3rdPartyResponses();

        String expectedResponse = "{\n"
                                  + "  \"task_id\": \"" + testTaskId + "\",\n"
                                  + "  \"case_id\": \"" + testCaseId + "\",\n"
                                  + "  \"assignee\": \"" + testUserId + "\",\n"
                                  + "  \"configuration_variables\": {\n"
                                  + "    \"caseTypeId\": \"Asylum\",\n"
                                  + "    \"taskState\": \"assigned\",\n"
                                  + "    \"executionType\": \"Case Management Task\",\n"
                                  + "    \"caseId\": \"" + testCaseId + "\",\n"
                                  + "    \"securityClassification\": \"PUBLIC\",\n"
                                  + "    \"autoAssigned\": true,\n"
                                  + "    \"taskSystem\": \"SELF\",\n"
                                  + "    \"title\": \"taskName\",\n"
                                  + "    \"hasWarnings\": false\n"
                                  + "  }\n"
                                  + "}";


        Map<String, Object> requiredProcessVariables = Map.of(
            CASE_ID.value(), testCaseId,
            NAME.value(), TASK_NAME
        );

        mockMvc.perform(
            post("/task/" + testTaskId + "/configuration")
                .contentType(APPLICATION_JSON_VALUE)
                .content(asJsonString(new ConfigureTaskRequest(requiredProcessVariables)))
        )
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse))
            .andReturn();

    }


    @DisplayName("Should return 200 and return configuration as body with no auto-assignment")
    @Test
    void should_succeed_and_return_configuration_with_n_auto_assignment() throws Exception {

        setupRoleAssignmentResponse(false);
        configure3rdPartyResponses();

        String expectedResponse = "{\n"
                                  + "  \"task_id\": \"" + testTaskId + "\",\n"
                                  + "  \"case_id\": \"" + testCaseId + "\",\n"
                                  + "  \"configuration_variables\": {\n"
                                  + "    \"caseTypeId\": \"Asylum\",\n"
                                  + "    \"taskState\": \"unassigned\",\n"
                                  + "    \"executionType\": \"Case Management Task\",\n"
                                  + "    \"caseId\": \"" + testCaseId + "\",\n"
                                  + "    \"securityClassification\": \"PUBLIC\",\n"
                                  + "    \"autoAssigned\": false,\n"
                                  + "    \"taskSystem\": \"SELF\",\n"
                                  + "    \"title\": \"taskName\",\n"
                                  + "    \"name1\": \"value1\",\n"
                                  + "    \"hasWarnings\": false\n"
                                  + "  }\n"
                                  + "}";

        Map<String, Object> requiredProcessVariables = Map.of(
            CASE_ID.value(), testCaseId,
            NAME.value(), TASK_NAME
        );

        mockMvc.perform(
            post("/task/" + testTaskId + "/configuration")
                .contentType(APPLICATION_JSON_VALUE)
                .content(asJsonString(new ConfigureTaskRequest(requiredProcessVariables)))
        )
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse))
            .andReturn();
    }


    private void setupRoleAssignmentResponse(boolean shouldReturnRoleAssignment) {

        List<RoleAssignment> roleAssignments = new ArrayList<>();

        if (shouldReturnRoleAssignment) {

            RoleAssignment roleAssignment = RoleAssignment.builder()
                .id("someId")
                .actorIdType(ActorIdType.IDAM)
                .actorId(testUserId)
                .roleName(RoleName.TRIBUNAL_CASEWORKER)
                .roleCategory(RoleCategory.STAFF)
                .roleType(RoleType.ORGANISATION)
                .classification(Classification.PUBLIC)
                .build();

            roleAssignments.add(roleAssignment);
        }

        when(roleAssignmentServiceApi.queryRoleAssignments(
            eq(BEARER_USER_TOKEN),
            eq(BEARER_SERVICE_TOKEN),
            any(QueryRequest.class)
        )).thenReturn(new RoleAssignmentResource(roleAssignments, null));

    }

    private HashMap<String, CamundaValue<String>> configure3rdPartyResponses() {

        when(camundaServiceApi.getTask(BEARER_SERVICE_TOKEN, testTaskId))
            .thenReturn(new CamundaTask(testTaskId, testProcessInstanceId, TASK_NAME));

        Map<String, CamundaValue<Object>> processVariables = Map.of(
            CASE_ID.value(), new CamundaValue<>(testCaseId, "String"),
            TASK_STATE.value(), new CamundaValue<>(UNCONFIGURED, "String")
        );

        when(camundaServiceApi.getVariables(BEARER_SERVICE_TOKEN, testTaskId))
            .thenReturn(processVariables);

        when(idamServiceApi.token(ArgumentMatchers.<Map<String, Object>>any()))
            .thenReturn(new Token(BEARER_USER_TOKEN, "scope"));

        when(systemTokenGenerator.generate()).thenReturn(BEARER_USER_TOKEN);
        when(serviceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        String caseData = "{ "
                          + "\"jurisdiction\": \"IA\", "
                          + "\"case_type\": \"Asylum\", "
                          + "\"security_classification\": \"PUBLIC\","
                          + "\"data\": {}"
                          + " }";

        when(ccdDataServiceApi.getCase(
            BEARER_USER_TOKEN,
            BEARER_SERVICE_TOKEN,
            testCaseId)
        ).thenReturn(caseData);

        when(camundaServiceApi.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_CONFIGURATION_DECISION_TABLE_NAME,
            "ia",
            "asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(caseData)))
            )
        ).thenReturn(singletonList(new DecisionTableResult(stringValue("name1"), stringValue("value1"))));

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("name1", stringValue("value1"));
        modifications.put("caseId", stringValue(testCaseId));
        modifications.put(TASK_STATE.value(), stringValue("configured"));
        modifications.put("autoAssigned", stringValue("false"));
        modifications.put("executionType", stringValue("Case Management Task"));
        modifications.put("securityClassification", stringValue("PUBLIC"));
        modifications.put("taskSystem", stringValue("SELF"));
        modifications.put("caseTypeId", stringValue("Asylum"));
        modifications.put("title", stringValue(TASK_NAME));
        modifications.put("hasWarnings", stringValue("false"));
        return modifications;
    }
}
