package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;

import java.util.Map;

import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.AUTO_ASSIGNED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.EXECUTION_TYPE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_SYSTEM;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.CONFIGURED;

@Component
@Order(1)
public class ConstantVariablesConfigurator implements TaskConfigurator {

    @Override
    public Map<String, Object> getConfigurationVariables(TaskToConfigure task) {

        return Map.of(
            TASK_STATE.value(), CONFIGURED.value(),
            AUTO_ASSIGNED.value(), false,
            EXECUTION_TYPE.value(), "Case Management Task",
            TASK_SYSTEM.value(), "SELF");
    }
}
