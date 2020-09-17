package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue.stringValue;

@Component
public class ConfigureTaskService {
    public static final String CCD_ID_PROCESS_VARIABLE_KEY = "ccdId";
    private final CamundaClient camundaClient;
    private final MapCaseDetailsService mapCaseDetailsService;

    public ConfigureTaskService(CamundaClient camundaClient, MapCaseDetailsService mapCaseDetailsService) {
        this.camundaClient = camundaClient;
        this.mapCaseDetailsService = mapCaseDetailsService;
    }

    @SuppressWarnings({
        "PMD.DataflowAnomalyAnalysis"
    })
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
        if (!processVariables.containsKey(CCD_ID_PROCESS_VARIABLE_KEY)) {
            throw new IllegalStateException(
                "Task id ["
                + taskId
                + "] cannot be configured it has not been setup correctly. No ccdId process variable."
            );
        }
        CamundaValue<Object> ccdId = processVariables.get(CCD_ID_PROCESS_VARIABLE_KEY);
        Map<String, Object> mappedDetails = mapCaseDetailsService.getMappedDetails((String) ccdId.getValue());
        mappedDetails.put(CCD_ID_PROCESS_VARIABLE_KEY, ccdId.getValue());

        Map<String, CamundaValue<String>> map = mappedDetails.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            mappedDetail -> stringValue(mappedDetail.getValue().toString())
        ));

        camundaClient.addLocalVariablesToTask(taskId, new AddLocalVariableRequest(map));
    }
}
