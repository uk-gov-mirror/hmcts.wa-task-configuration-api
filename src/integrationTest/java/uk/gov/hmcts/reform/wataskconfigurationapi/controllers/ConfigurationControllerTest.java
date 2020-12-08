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
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CcdClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.IdamApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.Token;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.roleassignment.RoleAssignmentClient;

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
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.ConstantVariableExtractor.STATUS_VARIABLE_KEY;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.MapCaseDetailsService.WA_TASK_CONFIGURATION_DECISION_TABLE_NAME;
import static uk.gov.hmcts.reform.wataskconfigurationapi.controllers.util.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.stringValue;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ConfigurationControllerTest {

    public static final String TASK_NAME = "taskName";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CamundaClient camundaClient;

    @MockBean(name = "ccdServiceAuthTokenGenerator")
    private AuthTokenGenerator ccdServiceAuthTokenGenerator;
    @MockBean(name = "camundaServiceAuthTokenGenerator")
    private AuthTokenGenerator camundaServiceAuthTokenGenerator;

    @MockBean
    private CcdClient ccdClient;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private RoleAssignmentClient roleAssignmentClient;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

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

        verify(camundaClient).addLocalVariablesToTask(
            BEARER_SERVICE_TOKEN,
            taskId,
            new AddLocalVariableRequest(modifications)
        );
    }

    @DisplayName("Cannot find task")
    @Test
    void cannotFindTask() throws Exception {
        String taskId = UUID.randomUUID().toString();

        when(camundaClient.getTask(BEARER_SERVICE_TOKEN, taskId)).thenThrow(mock(FeignException.NotFound.class));
        when(ccdServiceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
        when(camundaServiceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        mockMvc.perform(
            post("/configureTask")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(new ConfigureTaskRequest(taskId)))
        ).andExpect(status().isNotFound()).andReturn();

        verify(camundaClient, never()).addLocalVariablesToTask(
            eq(BEARER_SERVICE_TOKEN),
            any(String.class),
            any(AddLocalVariableRequest.class)
        );
    }

    private HashMap<String, CamundaValue<String>> configure3rdPartyResponses(String taskId, String processInstanceId) {

        String userToken = "user_token";
        when(roleAssignmentClient.queryRoleAssignments(
            eq("Bearer " + userToken),
            eq(BEARER_SERVICE_TOKEN),
            any(QueryRequest.class)
        )).thenReturn(new RoleAssignmentResource(emptyList(), null));

        when(camundaClient.getTask(BEARER_SERVICE_TOKEN, taskId))
            .thenReturn(new TaskResponse("id", processInstanceId, TASK_NAME));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String caseId = UUID.randomUUID().toString();
        processVariables.put("caseId", new CamundaValue<>(caseId, "string"));
        when(camundaClient.getProcessVariables(BEARER_SERVICE_TOKEN, processInstanceId)).thenReturn(processVariables);
        when(idamApi.token(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(new Token(userToken, "scope"));
        when(ccdServiceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
        when(camundaServiceAuthTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
        String caseData = "{ "
                          + "\"jurisdiction\": \"ia\", "
                          + "\"case_type_id\": \"Asylum\", "
                          + "\"security_classification\": \"PUBLIC\","
                          + "\"data\": {}"
                          + " }";
        when(ccdClient.getCase("Bearer " + userToken, BEARER_SERVICE_TOKEN, caseId)).thenReturn(caseData);
        when(camundaClient.evaluateDmnTable(
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
