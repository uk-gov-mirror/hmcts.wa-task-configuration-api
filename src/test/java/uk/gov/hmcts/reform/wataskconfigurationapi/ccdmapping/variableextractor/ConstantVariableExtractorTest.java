package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.ConstantVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.ConstantVariableExtractor.STATUS_VARIABLE_KEY;

class ConstantVariableExtractorTest {
    private TaskResponse task;

    @BeforeEach
    void setUp() {
        task = new TaskResponse("id", "processInstanceId", "taskName");
    }

    @Test
    void addsCcdId() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();

        HashMap<String, Object> expectedValues = new HashMap<>();
        expectedValues.put(STATUS_VARIABLE_KEY, "configured");
        expectedValues.put("autoAssigned", false);
        expectedValues.put("executionType", "Case Management Task");
        expectedValues.put("taskSystem", "SELF");

        Map<String, Object> values = new ConstantVariableExtractor().getValues(task, processVariables);

        assertThat(values, is(expectedValues));
    }
}
