package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.Map;

public interface TaskVariableExtractor {
    Map<String, Object> getValues(TaskResponse task, Map<String, CamundaValue<Object>> processVariables);
}
