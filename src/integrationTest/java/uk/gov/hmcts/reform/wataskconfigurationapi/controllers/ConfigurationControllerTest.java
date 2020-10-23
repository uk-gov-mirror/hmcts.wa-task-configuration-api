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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.ConstantVariableExtractor.STATUS_VARIABLE_KEY;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.MapCaseDetailsService.MAP_CASE_DATA_DECISION_TABLE_NAME;
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

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CcdClient ccdClient;

    @MockBean
    private IdamApi idamApi;

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

        verify(camundaClient).addLocalVariablesToTask(taskId, new AddLocalVariableRequest(modifications));
    }

    @DisplayName("Cannot find task")
    @Test
    void cannotFindTask() throws Exception {
        String taskId = UUID.randomUUID().toString();

        when(camundaClient.getTask(taskId)).thenThrow(mock(FeignException.NotFound.class));

        mockMvc.perform(
            post("/configureTask")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(new ConfigureTaskRequest(taskId)))
        ).andExpect(status().isNotFound()).andReturn();

        verify(camundaClient, never()).addLocalVariablesToTask(any(String.class), any(AddLocalVariableRequest.class));
    }

    private HashMap<String, CamundaValue<String>> configure3rdPartyResponses(String taskId, String processInstanceId) {
        when(camundaClient.getTask(taskId)).thenReturn(new TaskResponse("id", processInstanceId, TASK_NAME));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String ccdId = UUID.randomUUID().toString();
        processVariables.put("ccdId", new CamundaValue<>(ccdId, "string"));
        when(camundaClient.getProcessVariables(processInstanceId)).thenReturn(processVariables);
        String userToken = "user_token";
        when(idamApi.token(ArgumentMatchers.<Map<String, Object>>any())).thenReturn(new Token(userToken, "scope"));
        String serviceToken = "service_token";
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        String caseData = "{ "
                          + "\"jurisdiction\": \"ia\", "
                          + "\"case_type_id\": \"Asylum\", "
                          + "\"security_classification\": \"PUBLIC\","
                          + "\"data\": {}"
                          + " }";
        when(ccdClient.getCase("Bearer " + userToken, serviceToken, ccdId)).thenReturn(caseData);
        when(camundaClient.mapCaseData(
            MAP_CASE_DATA_DECISION_TABLE_NAME,
            "ia",
            "Asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(caseData)))
             )
        ).thenReturn(singletonList(new DecisionTableResult(stringValue("name1"), stringValue("value1"))));

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("name1", stringValue("value1"));
        modifications.put("ccdId", stringValue(ccdId));
        modifications.put(STATUS_VARIABLE_KEY, stringValue("configured"));
        modifications.put("autoAssigned", stringValue("false"));
        modifications.put("executionType", stringValue("Case Management Task"));
        modifications.put("securityClassification", stringValue("PUBLIC"));
        modifications.put("taskSystem", stringValue("SELF"));
        modifications.put("caseType", stringValue("Asylum"));
        modifications.put("title", stringValue(TASK_NAME));
        return modifications;
    }
}
