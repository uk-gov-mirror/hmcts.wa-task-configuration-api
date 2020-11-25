package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.MapCaseDetailsService;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.SystemConfiguredVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemConfiguredVariableExtractorTest {

    private TaskResponse task;
    private MapCaseDetailsService mapCaseDetailsService;
    private SystemConfiguredVariableExtractor systemConfiguredVariableExtractor;

    @BeforeEach
    void setUp() {
        task = new TaskResponse("id", "processInstanceId", "taskName");
        mapCaseDetailsService = mock(MapCaseDetailsService.class);

        systemConfiguredVariableExtractor = new SystemConfiguredVariableExtractor(mapCaseDetailsService);
    }

    @Test
    void taskNeedsACaseId() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();

        assertThrows(IllegalStateException.class, () -> {
            systemConfiguredVariableExtractor.getValues(task, processVariables);
        });
    }

    @Test
    void getsValuesFromMapCaseDetailsService() {
        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        String caseId = "ccd_id_123";
        processVariables.put(ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY, new CamundaValue<>(caseId, "String"));
        HashMap<String, Object> expectedValues = new HashMap<>();
        when(mapCaseDetailsService.getMappedDetails(caseId)).thenReturn(expectedValues);

        Map<String, Object> values = systemConfiguredVariableExtractor.getValues(task, processVariables);

        assertThat(values, sameInstance(expectedValues));
    }
}
