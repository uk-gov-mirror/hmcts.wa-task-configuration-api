package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.Map;

public class SystemConfiguredVariableExtractor implements TaskVariableExtractor {

    private final MapCaseDetailsService mapCaseDetailsService;

    public SystemConfiguredVariableExtractor(MapCaseDetailsService mapCaseDetailsService) {
        this.mapCaseDetailsService = mapCaseDetailsService;
    }

    @Override
    public Map<String, Object> getValues(TaskResponse task, Map<String, CamundaValue<Object>> processVariables) {
        if (!processVariables.containsKey(ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY)) {
            throw new IllegalStateException(
                "Task id ["
                + task.getId()
                + "] cannot be configured it has not been setup correctly. No ccdId process variable."
            );
        }
        CamundaValue<Object> ccdId = processVariables.get(ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY);
        return mapCaseDetailsService.getMappedDetails((String) ccdId.getValue());
    }
}
