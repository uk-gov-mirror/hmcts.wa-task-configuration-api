package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;

import java.util.Map;

public interface TaskConfigurator {
    Map<String, Object> getConfigurationVariables(TaskToConfigure taskToConfigure);
}
