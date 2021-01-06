package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TITLE;

@Component
@Order(2)
public class RequiredProcessVariableConfigurator implements TaskConfigurator {

    @Override
    public Map<String, Object> getConfigurationVariables(TaskToConfigure task) {

        requireNonNull(task.getCaseId(), String.format(
            "Task with id '%s' cannot be configured it has not been setup correctly. No caseId process variable.",
            task.getId()
        ));

        return Map.of(
            CASE_ID.value(), task.getCaseId(),
            TITLE.value(), task.getName()
        );
    }
}
