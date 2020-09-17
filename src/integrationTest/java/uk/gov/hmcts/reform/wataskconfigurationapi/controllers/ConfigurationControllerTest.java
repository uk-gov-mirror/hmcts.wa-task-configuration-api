package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CcdDataService;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.MapCaseDataDmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.MapCaseDataDmnResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.TaskResponse;

import java.util.HashMap;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue.stringValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.controllers.util.CreatorObjectMapper.asJsonString;

@SpringBootTest
@AutoConfigureMockMvc
public class ConfigurationControllerTest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private CamundaClient camundaClient;

    //todo this should mock the call to ccd not the service once that is implemented
    @MockBean
    private CcdDataService ccdDataService;

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
        when(camundaClient.getTask(taskId)).thenReturn(new TaskResponse("id", processInstanceId));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String ccdId = UUID.randomUUID().toString();
        processVariables.put("ccdId", new CamundaValue<>(ccdId, "string"));
        when(camundaClient.getProcessVariables(processInstanceId)).thenReturn(processVariables);
        String caseData = "{ case_Data: {} }";
        when(ccdDataService.getCaseData(ccdId)).thenReturn(caseData);
        when(camundaClient.mapCaseData(new DmnRequest<>(new MapCaseDataDmnRequest(jsonValue(caseData))))).thenReturn(
            singletonList(new MapCaseDataDmnResult(stringValue("name1"), stringValue("value1")))
        );
        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("name1", stringValue("value1"));
        modifications.put("ccdId", stringValue(ccdId));
        return modifications;
    }
}
