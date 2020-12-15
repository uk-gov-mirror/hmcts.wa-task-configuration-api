package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.ConfigureTaskService;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
@Component
@Order(2)
public class CamundaProcessVariableExtractor implements TaskConfigurator {

    @Override
    public Map<String, Object> getConfigurationVariables(CamundaTask task,
                                                         Map<String, CamundaValue<Object>> processVariables) {
        CamundaValue<Object> caseId = processVariables.get(ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY);
        Map<String, Object> mappedDetails = new HashMap<>();
        mappedDetails.put(ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY, caseId.getValue());
        mappedDetails.put("title", task.getName());
        return mappedDetails;
    }
}
