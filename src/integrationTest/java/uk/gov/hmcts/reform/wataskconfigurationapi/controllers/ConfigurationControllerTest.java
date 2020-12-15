package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.Token;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CcdDataServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.IdamServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignmentResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskconfigurationapi.controllers.util.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.stringValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.MapCaseDetailsService.WA_TASK_CONFIGURATION_DECISION_TABLE_NAME;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators.ConstantVariableExtractor.STATUS_VARIABLE_KEY;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ConfigurationControllerTest {

    public static final String TASK_NAME = "taskName";
    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CamundaServiceApi camundaServiceApi;
    @MockBean
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @MockBean
    private CcdDataServiceApi ccdDataServiceApi;
    @MockBean
    private IdamServiceApi idamServiceApi;
    @MockBean
    private RoleAssignmentServiceApi roleAssignmentServiceApi;

    @DisplayName("Should configure task")
    @Test
    void createsTaskForTransitionWithoutDueDate() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String processInstanceId = UUID.randomUUID().toString();

        HashMap<String, CamundaValue<String>> modifications = configure3rdPartyResponses(taskId, processInstanceId);

        mockMvc.perform(
            post("/configureTask")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(new ConfigureTaskRequest(taskId)))
        ).andExpect(status().isOk()).andReturn();

        verify(camundaServiceApi).addLocalVariablesToTask(
            BEARER_SERVICE_TOKEN,
            taskId,
            new AddLocalVariableRequest(modifications)
        );
    }

    @DisplayName("Cannot find task")
    @Test
    void cannotFindTask() throws Exception {
        String taskId = UUID.randomUUID().toString();

        when(camundaServiceApi.getTask(BEARER_SERVICE_TOKEN, taskId)).thenThrow(mock(FeignException.NotFound.class));
        when(serviceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        mockMvc.perform(
            post("/configureTask")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(new ConfigureTaskRequest(taskId)))
        ).andExpect(status().isNotFound()).andReturn();

        verify(camundaServiceApi, never()).addLocalVariablesToTask(
            eq(BEARER_SERVICE_TOKEN),
            any(String.class),
            any(AddLocalVariableRequest.class)
        );
    }

    private HashMap<String, CamundaValue<String>> configure3rdPartyResponses(String taskId, String processInstanceId) {

        String userToken = "user_token";
        when(roleAssignmentServiceApi.queryRoleAssignments(
            eq("Bearer " + userToken),
            eq(BEARER_SERVICE_TOKEN),
            any(QueryRequest.class)
        )).thenReturn(new RoleAssignmentResource(emptyList(), null));

        when(camundaServiceApi.getTask(BEARER_SERVICE_TOKEN, taskId))
            .thenReturn(new CamundaTask("id", processInstanceId, TASK_NAME));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String caseId = UUID.randomUUID().toString();
        processVariables.put("caseId", new CamundaValue<>(caseId, "string"));
        when(camundaServiceApi.getProcessVariables(BEARER_SERVICE_TOKEN, processInstanceId))
            .thenReturn(processVariables);
        when(idamServiceApi.token(ArgumentMatchers.<Map<String, Object>>any()))
            .thenReturn(new Token(userToken, "scope"));
        when(serviceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
        String caseData = "{ "
                          + "\"jurisdiction\": \"ia\", "
                          + "\"case_type\": \"Asylum\", "
                          + "\"security_classification\": \"PUBLIC\","
                          + "\"data\": {}"
                          + " }";
        when(ccdDataServiceApi.getCase("Bearer " + userToken, BEARER_SERVICE_TOKEN, caseId))
            .thenReturn(caseData);
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
        modifications.put("caseId", stringValue(caseId));
        modifications.put(STATUS_VARIABLE_KEY, stringValue("unassigned"));
        modifications.put("autoAssigned", stringValue("false"));
        modifications.put("executionType", stringValue("Case Management Task"));
        modifications.put("securityClassification", stringValue("PUBLIC"));
        modifications.put("taskSystem", stringValue("SELF"));
        modifications.put("caseType", stringValue("Asylum"));
        modifications.put("title", stringValue(TASK_NAME));
        return modifications;
    }
}
