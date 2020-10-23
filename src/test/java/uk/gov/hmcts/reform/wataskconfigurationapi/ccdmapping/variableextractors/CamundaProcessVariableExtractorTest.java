package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.CamundaProcessVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY;

class CamundaProcessVariableExtractorTest {

    public static final String TASK_NAME = "taskName";
    private TaskResponse task;
    private CamundaProcessVariableExtractor camundaProcessVariableExtractor;

    @BeforeEach
    void setUp() {
        task = new TaskResponse("id", "processInstanceId", TASK_NAME);
        camundaProcessVariableExtractor = new CamundaProcessVariableExtractor();
    }

    @Test
    void addsVariables() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(CCD_ID_PROCESS_VARIABLE_KEY, new CamundaValue<>("CCD_ID_123", "String"));

        Map<String, Object> values = camundaProcessVariableExtractor.getValues(task, processVariables);

        assertThat(values.size(), is(2));
        assertThat(values.get(CCD_ID_PROCESS_VARIABLE_KEY), is("CCD_ID_123"));
        assertThat(values.get("title"), is(TASK_NAME));
    }
}
