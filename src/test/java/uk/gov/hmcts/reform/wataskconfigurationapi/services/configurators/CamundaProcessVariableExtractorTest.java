package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY;

class CamundaProcessVariableExtractorTest {

    public static final String TASK_NAME = "taskName";
    private CamundaTask task;
    private CamundaProcessVariableExtractor camundaProcessVariableExtractor;

    @BeforeEach
    void setUp() {
        task = new CamundaTask("id", "processInstanceId", TASK_NAME);
        camundaProcessVariableExtractor = new CamundaProcessVariableExtractor();
    }

    @Test
    void addsVariables() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(CASE_ID_PROCESS_VARIABLE_KEY, new CamundaValue<>("CASE_ID_123", "String"));

        Map<String, Object> values = camundaProcessVariableExtractor.getConfigurationVariables(task, processVariables);

        assertThat(values.size(), is(2));
        assertThat(values.get(CASE_ID_PROCESS_VARIABLE_KEY), is("CASE_ID_123"));
        assertThat(values.get("title"), is(TASK_NAME));
    }
}
