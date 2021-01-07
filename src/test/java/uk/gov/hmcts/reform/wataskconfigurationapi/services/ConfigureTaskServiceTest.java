package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators.TaskConfigurator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.CONFIGURED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.UNCONFIGURED;

class ConfigureTaskServiceTest {

    private TaskToConfigure testTaskToConfigure;
    private CamundaService camundaService;
    private ConfigureTaskService configureTaskService;
    private TaskConfigurator taskVariableExtractor;
    private TaskAutoAssignmentService taskAutoAssignmentService;

    @BeforeEach
    void setup() {
        camundaService = mock(CamundaService.class);
        taskVariableExtractor = mock(TaskConfigurator.class);
        TaskAutoAssignmentService taskAutoAssignmentService = mock(TaskAutoAssignmentService.class);
        configureTaskService = new ConfigureTaskService(
            camundaService,
            Collections.singletonList(taskVariableExtractor),
            taskAutoAssignmentService
        );

        testTaskToConfigure = new TaskToConfigure(
            "taskId",
            "caseId",
            "taskName",
            Map.of(
                CASE_ID.value(), "caseId",
                TASK_STATE.value(), "unconfigured"
            )
        );
    }

    @Test
    void can_configure_a_task_with_variables() {

        String processInstanceId = "processInstanceId";

        CamundaTask camundaTask = new CamundaTask(
            testTaskToConfigure.getId(),
            processInstanceId,
            testTaskToConfigure.getName()
        );
        when(camundaService.getTask(testTaskToConfigure.getId())).thenReturn(camundaTask);

        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(
            CASE_ID.value(),
            new CamundaValue<>(testTaskToConfigure.getCaseId(), "String")
        );
        processVariables.put(
            TASK_STATE.value(),
            new CamundaValue<>(UNCONFIGURED.value(), "String")
        );
        when(camundaService.getVariables(testTaskToConfigure.getId()))
            .thenReturn(processVariables);

        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put("key1", "value1");
        mappedValues.put("key2", "value2");
        mappedValues.put(TASK_STATE.value(), CONFIGURED.value());

        when(taskVariableExtractor.getConfigurationVariables(testTaskToConfigure))
            .thenReturn(mappedValues);

        configureTaskService.configureTask(testTaskToConfigure.getId());

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("key1", CamundaValue.stringValue("value1"));
        modifications.put("key2", CamundaValue.stringValue("value2"));
        modifications.put(TASK_STATE.value(), CamundaValue.stringValue(CONFIGURED.value()));

        verify(camundaService).addProcessVariables(
            testTaskToConfigure.getId(),
            modifications
        );
    }

    @Test
    void can_configure_a_task_with_no_extra_variables() {

        String processInstanceId = "processInstanceId";

        CamundaTask camundaTask = new CamundaTask(
            testTaskToConfigure.getId(),
            processInstanceId,
            testTaskToConfigure.getName()
        );
        when(camundaService.getTask(testTaskToConfigure.getId())).thenReturn(camundaTask);

        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(
            CASE_ID.value(),
            new CamundaValue<>(testTaskToConfigure.getCaseId(), "String")
        );
        processVariables.put(
            TASK_STATE.value(),
            new CamundaValue<>(UNCONFIGURED.value(), "String")
        );
        when(camundaService.getVariables(testTaskToConfigure.getId()))
            .thenReturn(processVariables);

        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put(TASK_STATE.value(), CONFIGURED.value());

        when(taskVariableExtractor.getConfigurationVariables(testTaskToConfigure))
            .thenReturn(mappedValues);

        configureTaskService.configureTask(testTaskToConfigure.getId());

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put(TASK_STATE.value(), CamundaValue.stringValue(CONFIGURED.value()));

        verify(camundaService).addProcessVariables(
            testTaskToConfigure.getId(),
            modifications
        );
    }

    @Test
    void try_to_configure_a_task_that_does_not_exist() {
        String taskIdThatDoesNotExist = "doesNotExist";
        when(camundaService.getTask(taskIdThatDoesNotExist))
            .thenThrow(new ResourceNotFoundException("exception message", new Exception()));

        assertThatThrownBy(() -> configureTaskService.configureTask(taskIdThatDoesNotExist))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("exception message");
    }
}
