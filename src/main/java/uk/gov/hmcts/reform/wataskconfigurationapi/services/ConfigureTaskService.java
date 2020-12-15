package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.ConfigureTaskException;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators.TaskConfigurator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.stringValue;

@Component
public class ConfigureTaskService {
    public static final String CASE_ID_PROCESS_VARIABLE_KEY = "caseId";

    private final CamundaServiceApi camundaServiceApi;
    private final List<TaskConfigurator> taskConfigurators;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public ConfigureTaskService(CamundaServiceApi camundaServiceApi,
                                List<TaskConfigurator> taskConfigurators,
                                AuthTokenGenerator serviceAuthTokenGenerator) {
        this.camundaServiceApi = camundaServiceApi;
        this.taskConfigurators = taskConfigurators;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
    }

    @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
    public void configureTask(String taskId) {
        CamundaTask task;
        try {
            task = camundaServiceApi.getTask(serviceAuthTokenGenerator.generate(), taskId);
        } catch (FeignException.NotFound notFoundException) {
            throw new ConfigureTaskException(
                "Task [" + taskId + "] cannot be configured as it has not been found.",
                notFoundException
            );
        }

        Map<String, CamundaValue<Object>> processVariables = camundaServiceApi.getProcessVariables(
            serviceAuthTokenGenerator.generate(),
            task.getProcessInstanceId()
        );

        HashMap<String, Object> mappedDetails = new HashMap<>();
        taskConfigurators.stream()
            .map(taskVariableExtractor -> taskVariableExtractor.getConfigurationVariables(task, processVariables))
            .forEach(mappedDetails::putAll);

        Map<String, CamundaValue<String>> map = mappedDetails.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            mappedDetail -> stringValue(mappedDetail.getValue().toString())
        ));

        camundaServiceApi.addLocalVariablesToTask(
            serviceAuthTokenGenerator.generate(),
            taskId,
            new AddLocalVariableRequest(map)
        );
    }
}
