package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.ProcessVariablesVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY;

class ProcessVariablesVariableExtractorTest {

    private TaskResponse task;
    private ProcessVariablesVariableExtractor processVariablesVariableExtractor;

    @BeforeEach
    void setUp() {
        task = new TaskResponse("id", "processInstanceId");
        processVariablesVariableExtractor = new ProcessVariablesVariableExtractor();
    }

    @Test
    void addsCcdId() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(CCD_ID_PROCESS_VARIABLE_KEY, new CamundaValue<>("CCD_ID_123", "String"));

        Map<String, Object> values = processVariablesVariableExtractor.getValues(task, processVariables);

        assertThat(values.size(), is(1));
        assertThat(values.get(CCD_ID_PROCESS_VARIABLE_KEY), is("CCD_ID_123"));
    }

}
