package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.AUTO_ASSIGNED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.EXECUTION_TYPE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.HAS_WARNINGS;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_SYSTEM;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.CONFIGURED;

class ConstantVariablesConfiguratorTest {

    private ConstantVariablesConfigurator constantVariablesConfigurator;

    @BeforeEach
    void setUp() {
        constantVariablesConfigurator = new ConstantVariablesConfigurator();
    }

    @Test
    void should_add_constant_variables() {

        TaskToConfigure testTaskToConfigure = new TaskToConfigure(
            "taskId",
            "caseId",
            "taskName",
            emptyMap()
        );

        Map<String, Object> expectedValues = Map.of(
            TASK_STATE.value(), CONFIGURED.value(),
            AUTO_ASSIGNED.value(), false,
            EXECUTION_TYPE.value(), "Case Management Task",
            TASK_SYSTEM.value(), "SELF",
            HAS_WARNINGS.value(), false);


        Map<String, Object> values = constantVariablesConfigurator.getConfigurationVariables(testTaskToConfigure);

        assertThat(values, is(expectedValues));
    }
}
