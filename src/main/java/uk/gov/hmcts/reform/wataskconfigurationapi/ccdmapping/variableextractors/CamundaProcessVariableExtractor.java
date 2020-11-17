package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
@Component
@Order(2)
public class CamundaProcessVariableExtractor implements TaskVariableExtractor {
    @Override
    public Map<String, Object> getValues(TaskResponse task, Map<String, CamundaValue<Object>> processVariables) {
        CamundaValue<Object> ccdId = processVariables.get(ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY);
        Map<String, Object> mappedDetails = new HashMap<>();
        mappedDetails.put(ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY, ccdId.getValue());
        mappedDetails.put("title", task.getName());
        return mappedDetails;
    }
}
