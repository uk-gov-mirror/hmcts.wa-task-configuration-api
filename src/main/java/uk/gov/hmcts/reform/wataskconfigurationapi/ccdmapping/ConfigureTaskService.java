package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.TaskVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.stringValue;

@Component
public class ConfigureTaskService {
    public static final String CCD_ID_PROCESS_VARIABLE_KEY = "ccdId";

    private final CamundaClient camundaClient;
    private final List<TaskVariableExtractor> taskVariableExtractors;

    public ConfigureTaskService(CamundaClient camundaClient, List<TaskVariableExtractor> taskVariableExtractors) {
        this.camundaClient = camundaClient;
        this.taskVariableExtractors = taskVariableExtractors;
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
    public void configureTask(String taskId) {
        TaskResponse task;
        try {
            task = camundaClient.getTask(taskId);
        } catch (FeignException.NotFound notFoundException) {
            throw new ConfigureTaskException(
                "Task [" + taskId + "] cannot be configured as it has not been found.",
                notFoundException
            );
        }

        Map<String, CamundaValue<Object>> processVariables =
            camundaClient.getProcessVariables(task.getProcessInstanceId());

        HashMap<String, Object> mappedDetails = new HashMap<>();
        taskVariableExtractors.stream()
            .map(taskVariableExtractor -> taskVariableExtractor.getValues(task, processVariables))
            .forEach(mappedDetails::putAll);

        Map<String, CamundaValue<String>> map = mappedDetails.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            mappedDetail -> stringValue(mappedDetail.getValue().toString())
        ));

        camundaClient.addLocalVariablesToTask(taskId, new AddLocalVariableRequest(map));
    }
}
