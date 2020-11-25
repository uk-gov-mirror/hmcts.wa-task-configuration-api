package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.TaskVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigureTaskServiceTest {

    private CamundaClient camundaClient;
    private ConfigureTaskService configureTaskService;
    private TaskVariableExtractor taskVariableExtractor;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    @BeforeEach
    void setup() {
        camundaClient = mock(CamundaClient.class);
        taskVariableExtractor = mock(TaskVariableExtractor.class);
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
        configureTaskService = new ConfigureTaskService(
            camundaClient,
            Collections.singletonList(taskVariableExtractor),
            authTokenGenerator
        );

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
    }

    @Test
    void canConfigureATaskWithVariables() {
        String taskId = "taskId";
        String processInstanceId = "processInstanceId";
        TaskResponse taskResponse = new TaskResponse("id", processInstanceId, "taskName");
        when(camundaClient.getTask(BEARER_SERVICE_TOKEN, taskId)).thenReturn(taskResponse);

        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String caseId = "someCcdValue";
        processVariables.put("caseId", new CamundaValue<>(caseId, "String"));
        when(camundaClient.getProcessVariables(BEARER_SERVICE_TOKEN, processInstanceId)).thenReturn(processVariables);

        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put("key1", "value1");
        mappedValues.put("key2", "value2");
        when(taskVariableExtractor.getValues(taskResponse, processVariables)).thenReturn(mappedValues);

        configureTaskService.configureTask(taskId);

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("key1", CamundaValue.stringValue("value1"));
        modifications.put("key2", CamundaValue.stringValue("value2"));
        verify(camundaClient).addLocalVariablesToTask(
            BEARER_SERVICE_TOKEN,
            taskId,
            new AddLocalVariableRequest(modifications)
        );
    }

    @Test
    void canConfigureATaskWithNoExtraVariables() {
        String taskId = "taskId";
        String processInstanceId = "processInstanceId";
        TaskResponse taskResponse = new TaskResponse("id", processInstanceId, "taskName");
        when(camundaClient.getTask(BEARER_SERVICE_TOKEN, taskId)).thenReturn(taskResponse);
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String caseId = "someCcdValue";
        processVariables.put("caseId", new CamundaValue<>(caseId, "String"));
        when(camundaClient.getProcessVariables(BEARER_SERVICE_TOKEN, processInstanceId)).thenReturn(processVariables);
        HashMap<String, Object> mappedValues = new HashMap<>();
        when(taskVariableExtractor.getValues(taskResponse, processVariables)).thenReturn(mappedValues);

        configureTaskService.configureTask(taskId);

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        verify(camundaClient).addLocalVariablesToTask(
            BEARER_SERVICE_TOKEN,
            taskId,
            new AddLocalVariableRequest(modifications)
        );
    }

    @Test
    void tryToConfigureATaskThatDoesNotExist() {
        String taskIdThatDoesNotExist = "doesNotExist";
        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(camundaClient.getTask(BEARER_SERVICE_TOKEN, taskIdThatDoesNotExist)).thenThrow(notFound);

        Assertions.assertThrows(ConfigureTaskException.class, () -> {
            configureTaskService.configureTask(taskIdThatDoesNotExist);
        });
    }
}
