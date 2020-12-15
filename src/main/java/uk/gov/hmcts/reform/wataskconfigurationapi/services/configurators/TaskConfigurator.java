package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;

import java.util.Map;

public interface TaskConfigurator {
    Map<String, Object> getConfigurationVariables(CamundaTask task,
                                                  Map<String, CamundaValue<Object>> processVariables);
}
