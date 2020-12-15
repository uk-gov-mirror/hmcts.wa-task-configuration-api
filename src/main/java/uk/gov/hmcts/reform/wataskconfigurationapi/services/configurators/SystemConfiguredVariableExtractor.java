package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.ConfigureTaskService;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.MapCaseDetailsService;

import java.util.Map;

@Component
@Order(3)
public class SystemConfiguredVariableExtractor implements TaskConfigurator {

    private final MapCaseDetailsService mapCaseDetailsService;

    public SystemConfiguredVariableExtractor(MapCaseDetailsService mapCaseDetailsService) {
        this.mapCaseDetailsService = mapCaseDetailsService;
    }

    @Override
    public Map<String, Object> getConfigurationVariables(CamundaTask task,
                                                         Map<String, CamundaValue<Object>> processVariables) {
        if (!processVariables.containsKey(ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY)) {
            throw new IllegalStateException(
                "Task id ["
                + task.getId()
                + "] cannot be configured it has not been setup correctly. No caseId process variable."
            );
        }
        CamundaValue<Object> caseId = processVariables.get(ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY);
        return mapCaseDetailsService.getMappedDetails((String) caseId.getValue());
    }
}
