package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
@Component
@Order(1)
public class ConstantVariableExtractor implements TaskVariableExtractor {
    public static final String STATUS_VARIABLE_KEY = "taskState";

    @Override
    public Map<String, Object> getValues(TaskResponse task, Map<String, CamundaValue<Object>> processVariables) {
        Map<String, Object> mappedDetails = new HashMap<>();
        mappedDetails.put(STATUS_VARIABLE_KEY, "configured");
        mappedDetails.put("autoAssigned", false);
        mappedDetails.put("executionType", "Case Management Task");
        mappedDetails.put("taskSystem", "SELF");

        return mappedDetails;
    }
}
