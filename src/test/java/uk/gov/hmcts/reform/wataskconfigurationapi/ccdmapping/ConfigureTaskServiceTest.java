package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigureTaskServiceTest {

    private CamundaClient camundaClient;
    private MapCaseDetailsService mapCaseDetailsService;
    private ConfigureTaskService configureTaskService;

    @BeforeEach
    void setup() {
        camundaClient = mock(CamundaClient.class);
        mapCaseDetailsService = mock(MapCaseDetailsService.class);
        configureTaskService = new ConfigureTaskService(camundaClient, mapCaseDetailsService);
    }

    @Test
    void canConfigureATaskWithVariables() {
        String taskId = "taskId";
        String processInstanceId = "processInstanceId";
        when(camundaClient.getTask(taskId)).thenReturn(new TaskResponse("id", processInstanceId));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String ccdId = "someCcdValue";
        processVariables.put("ccdId", new CamundaValue<>(ccdId, "String"));
        when(camundaClient.getProcessVariables(processInstanceId)).thenReturn(processVariables);
        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put("key1", "value1");
        mappedValues.put("key2", "value2");
        when(mapCaseDetailsService.getMappedDetails(ccdId)).thenReturn(mappedValues);

        configureTaskService.configureTask(taskId);

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("key1", CamundaValue.stringValue("value1"));
        modifications.put("key2", CamundaValue.stringValue("value2"));
        modifications.put("ccdId", CamundaValue.stringValue(ccdId));
        modifications.put("status", CamundaValue.stringValue("configured"));
        verify(camundaClient).addLocalVariablesToTask(taskId, new AddLocalVariableRequest(modifications));
    }

    @Test
    void canConfigureATaskWithNoExtraVariables() {
        String taskId = "taskId";
        String processInstanceId = "processInstanceId";
        when(camundaClient.getTask(taskId)).thenReturn(new TaskResponse("id", processInstanceId));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String ccdId = "someCcdValue";
        processVariables.put("ccdId", new CamundaValue<>(ccdId, "String"));
        when(camundaClient.getProcessVariables(processInstanceId)).thenReturn(processVariables);
        HashMap<String, Object> mappedValues = new HashMap<>();
        when(mapCaseDetailsService.getMappedDetails(ccdId)).thenReturn(mappedValues);

        configureTaskService.configureTask(taskId);

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("ccdId", CamundaValue.stringValue(ccdId));
        modifications.put("status", CamundaValue.stringValue("configured"));
        verify(camundaClient).addLocalVariablesToTask(taskId, new AddLocalVariableRequest(modifications));
    }

    @Test
    void tryToConfigureATaskThatDoesNotExist() {
        String taskIdThatDoesNotExist = "doesNotExist";
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(camundaClient.getTask(taskIdThatDoesNotExist)).thenThrow(notFound);

        Assertions.assertThrows(ConfigureTaskException.class, () -> {
            configureTaskService.configureTask(taskIdThatDoesNotExist);
        });
    }

    @Test
    void tryToConfigureATaskThatDoesNotHaveACcdId() {
        String taskId = "taskId";
        when(camundaClient.getTask(taskId)).thenReturn(new TaskResponse("id", taskId));
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        when(camundaClient.getProcessVariables(taskId)).thenReturn(processVariables);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            configureTaskService.configureTask(taskId);
        });
    }
}
