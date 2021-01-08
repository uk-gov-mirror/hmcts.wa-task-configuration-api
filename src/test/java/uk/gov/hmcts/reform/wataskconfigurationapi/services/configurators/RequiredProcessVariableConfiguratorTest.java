package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TITLE;

class RequiredProcessVariableConfiguratorTest {

    private RequiredProcessVariableConfigurator requiredProcessVariableConfigurator;

    @BeforeEach
    void setUp() {
        requiredProcessVariableConfigurator = new RequiredProcessVariableConfigurator();
    }

    @Test
    void should_add_variables() {

        TaskToConfigure testTaskToConfigure = new TaskToConfigure(
            "taskId",
            "CASE_ID_123",
            "taskName",
            emptyMap()
        );

        Map<String, Object> values = requiredProcessVariableConfigurator.getConfigurationVariables(testTaskToConfigure);

        assertThat(values.size(), is(2));
        assertThat(values.get(CASE_ID.value()), is("CASE_ID_123"));
        assertThat(values.get(TITLE.value()), is("taskName"));
    }
}
