package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AssigneeRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.TaskState;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.ResourceNotFoundException;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.LawOfDemeter")
@Service
public class CamundaService {

    private final CamundaServiceApi camundaServiceApi;
    private final AuthTokenGenerator serviceTokenGenerator;

    @Autowired
    public CamundaService(CamundaServiceApi camundaServiceApi,
                          AuthTokenGenerator serviceTokenGenerator
    ) {
        this.camundaServiceApi = camundaServiceApi;
        this.serviceTokenGenerator = serviceTokenGenerator;
    }

    public CamundaTask getTask(String taskId) {
        requireNonNull(taskId, "taskId cannot be null");
        return performGetCamundaTaskAction(taskId);
    }

    public Map<String, CamundaValue<Object>> getVariables(String taskId) {
        requireNonNull(taskId, "taskId cannot be null");
        return performGetVariablesAction(taskId);
    }

    public void addProcessVariables(String taskId, Map<String, CamundaValue<String>> processVariablesToAdd) {
        requireNonNull(taskId, "taskId cannot be null");
        addLocalVariablesToTask(taskId, processVariablesToAdd);
    }

    public void assignTask(String taskId, String assigneeId, String currentTaskState) {
        requireNonNull(assigneeId, "assigneeId cannot be null");

        boolean taskStateIsAssignedAlready = TaskState.ASSIGNED.value().equals(currentTaskState);

        performAssignTaskAction(
            taskId,
            assigneeId,
            taskStateIsAssignedAlready
        );
    }

    private Map<String, CamundaValue<Object>> performGetVariablesAction(String id) {
        try {
            return camundaServiceApi.getVariables(serviceTokenGenerator.generate(), id);
        } catch (FeignException ex) {
            throw new ResourceNotFoundException(String.format(
                "There was a problem fetching the variables for task with id: %s",
                id
            ), ex);
        }
    }

    private CamundaTask performGetCamundaTaskAction(String id) {
        try {
            return camundaServiceApi.getTask(serviceTokenGenerator.generate(), id);
        } catch (FeignException ex) {
            throw new ResourceNotFoundException(String.format(
                "There was a problem fetching the task with id: %s",
                id
            ), ex);
        }
    }

    private void addLocalVariablesToTask(String taskId, Map<String, CamundaValue<String>> processVariablesToAdd) {
        try {
            camundaServiceApi.addLocalVariablesToTask(
                serviceTokenGenerator.generate(),
                taskId,
                new AddLocalVariableRequest(processVariablesToAdd)
            );
        } catch (FeignException ex) {
            throw new ResourceNotFoundException(String.format(
                "There was a problem updating process variables for task with id: %s",
                taskId
            ), ex);
        }
    }

    private void performAssignTaskAction(String taskId,
                                         String userId,
                                         boolean taskStateIsAssignedAlready) {
        try {
            if (!taskStateIsAssignedAlready) {
                updateTaskStateTo(taskId, TaskState.ASSIGNED);
            }
            camundaServiceApi.assignTask(serviceTokenGenerator.generate(), taskId, new AssigneeRequest(userId));
        } catch (FeignException ex) {
            throw new ServerErrorException(
                String.format(
                    "There was a problem assigning the task with id: %s",
                    taskId
                ), ex);
        }
    }

    public void updateTaskStateTo(String taskId, TaskState newState) {
        Map<String, CamundaValue<String>> variable = Map.of(
            CamundaVariableDefinition.TASK_STATE.value(), CamundaValue.stringValue(newState.value())
        );
        AddLocalVariableRequest camundaLocalVariables = new AddLocalVariableRequest(variable);
        camundaServiceApi.addLocalVariablesToTask(serviceTokenGenerator.generate(), taskId, camundaLocalVariables);
    }

}
