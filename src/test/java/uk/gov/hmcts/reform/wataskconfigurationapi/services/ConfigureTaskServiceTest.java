package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.response.ConfigureTaskResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.AutoAssignmentResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators.TaskConfigurator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.TASK_STATE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.CONFIGURED;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState.UNCONFIGURED;

class ConfigureTaskServiceTest {

    private static final String ASSIGNEE = "assignee1";
    private TaskToConfigure task;
    private CamundaService camundaService;
    private ConfigureTaskService configureTaskService;
    private TaskConfigurator taskVariableExtractor;
    private TaskAutoAssignmentService service;

    @BeforeEach
    void setup() {
        camundaService = mock(CamundaService.class);
        taskVariableExtractor = mock(TaskConfigurator.class);
        service = mock(TaskAutoAssignmentService.class);
        configureTaskService = new ConfigureTaskService(
            camundaService,
            Collections.singletonList(taskVariableExtractor),
            service
        );

        task = new TaskToConfigure(
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
            task.getId(),
            processInstanceId,
            task.getName()
        );
        when(camundaService.getTask(task.getId())).thenReturn(camundaTask);

        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(
            CASE_ID.value(),
            new CamundaValue<>(task.getCaseId(), "String")
        );
        processVariables.put(
            TASK_STATE.value(),
            new CamundaValue<>(UNCONFIGURED.value(), "String")
        );

        when(camundaService.getVariables(task.getId())).thenReturn(processVariables);

        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put("key1", "value1");
        mappedValues.put("key2", "value2");
        mappedValues.put(TASK_STATE.value(), CONFIGURED.value());

        when(taskVariableExtractor.getConfigurationVariables(task))
            .thenReturn(mappedValues);

        configureTaskService.configureTask(task.getId());

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put("key1", CamundaValue.stringValue("value1"));
        modifications.put("key2", CamundaValue.stringValue("value2"));
        modifications.put(TASK_STATE.value(), CamundaValue.stringValue(CONFIGURED.value()));

        verify(camundaService).addProcessVariables(
            task.getId(),
            modifications
        );
    }

    @Test
    void can_configure_a_task_with_no_extra_variables() {

        String processInstanceId = "processInstanceId";

        CamundaTask camundaTask = new CamundaTask(
            task.getId(),
            processInstanceId,
            task.getName()
        );
        when(camundaService.getTask(task.getId())).thenReturn(camundaTask);

        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put(
            CASE_ID.value(),
            new CamundaValue<>(task.getCaseId(), "String")
        );
        processVariables.put(
            TASK_STATE.value(),
            new CamundaValue<>(UNCONFIGURED.value(), "String")
        );

        when(camundaService.getVariables(task.getId()))
            .thenReturn(processVariables);

        HashMap<String, Object> mappedValues = new HashMap<>();
        mappedValues.put(TASK_STATE.value(), CONFIGURED.value());

        when(taskVariableExtractor.getConfigurationVariables(task))
            .thenReturn(mappedValues);

        configureTaskService.configureTask(task.getId());

        HashMap<String, CamundaValue<String>> modifications = new HashMap<>();
        modifications.put(TASK_STATE.value(), CamundaValue.stringValue(CONFIGURED.value()));

        verify(camundaService).addProcessVariables(
            task.getId(),
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

    @Test
    void should_get_configuration_with_assignee() {

        final AutoAssignmentResult autoAssignmentResult =
            new AutoAssignmentResult(
                TaskState.ASSIGNED.value(),
                "assignee1");

        when(service
            .getAutoAssignmentVariables(task))
            .thenReturn(autoAssignmentResult);

        final ConfigureTaskResponse configureTaskResponse =
            configureTaskService.getConfiguration(task);

        assertNotNull(configureTaskResponse);
        assertEquals(configureTaskResponse.getTaskId(), task.getId());
        assertEquals(configureTaskResponse.getCaseId(), task.getCaseId());
        assertEquals(ASSIGNEE, configureTaskResponse.getAssignee());
    }

    @Test
    void should_get_configuration_with_no_assignee() {

        final AutoAssignmentResult result = mock(AutoAssignmentResult.class);

        when(service
            .getAutoAssignmentVariables(task))
            .thenReturn(result);

        final ConfigureTaskResponse configureTaskResponse =
            configureTaskService.getConfiguration(task);

        assertNotNull(configureTaskResponse);
        assertEquals(configureTaskResponse.getTaskId(), task.getId());
        assertEquals(configureTaskResponse.getCaseId(), task.getCaseId());
    }
}
