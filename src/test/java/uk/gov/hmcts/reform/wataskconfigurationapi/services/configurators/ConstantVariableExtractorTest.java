package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators.ConstantVariableExtractor.STATUS_VARIABLE_KEY;

class ConstantVariableExtractorTest {
    private CamundaTask task;

    @BeforeEach
    void setUp() {
        task = new CamundaTask("id", "processInstanceId", "taskName");
    }

    @Test
    void addsCaseId() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();

        HashMap<String, Object> expectedValues = new HashMap<>();
        expectedValues.put(STATUS_VARIABLE_KEY, "configured");
        expectedValues.put("autoAssigned", false);
        expectedValues.put("executionType", "Case Management Task");
        expectedValues.put("taskSystem", "SELF");

        Map<String, Object> values = new ConstantVariableExtractor().getConfigurationVariables(task, processVariables);

        assertThat(values, is(expectedValues));
    }
}
