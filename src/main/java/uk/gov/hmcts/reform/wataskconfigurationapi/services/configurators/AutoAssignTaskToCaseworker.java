package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AssigneeRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.TaskState;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.ConfigureTaskService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(4)
public class AutoAssignTaskToCaseworker implements TaskConfigurator {

    private final RoleAssignmentServiceApi roleAssignmentServiceApi;
    private final CamundaServiceApi camundaServiceApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamSystemTokenGenerator idamSystemTokenGenerator;

    public AutoAssignTaskToCaseworker(RoleAssignmentServiceApi roleAssignmentServiceApi,
                                      CamundaServiceApi camundaServiceApi,
                                      AuthTokenGenerator serviceAuthTokenGenerator,
                                      IdamSystemTokenGenerator idamSystemTokenGenerator) {
        this.roleAssignmentServiceApi = roleAssignmentServiceApi;
        this.camundaServiceApi = camundaServiceApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.idamSystemTokenGenerator = idamSystemTokenGenerator;
    }

    @Override
    public Map<String, Object> getConfigurationVariables(CamundaTask task,
                                                         Map<String, CamundaValue<Object>> processVariables) {
        String caseId = (String) processVariables.get(ConfigureTaskService.CASE_ID_PROCESS_VARIABLE_KEY).getValue();

        RoleAssignmentResource roleAssignmentList = roleAssignmentServiceApi.queryRoleAssignments(
            idamSystemTokenGenerator.generate(),
            serviceAuthTokenGenerator.generate(),
            buildQueryRequest(caseId)
        );


        return updateTaskStateAndSetAssignee(task, roleAssignmentList.getRoleAssignmentResponse());
    }

    @SuppressWarnings({"PMD.LawOfDemeter"})
    private Map<String, Object> updateTaskStateAndSetAssignee(CamundaTask task,
                                                              List<RoleAssignment> roleAssignmentList) {
        Map<String, Object> taskVariables = new ConcurrentHashMap<>();
        if (roleAssignmentList.isEmpty()) {
            taskVariables.put("taskState", TaskState.UNASSIGNED.getValue());
        } else {
            String actorId = roleAssignmentList.get(0).getActorId();
            camundaServiceApi.setAssignee(
                serviceAuthTokenGenerator.generate(),
                task.getId(),
                new AssigneeRequest(actorId)
            );
            taskVariables.put("taskState", TaskState.ASSIGNED.getValue());
        }
        return taskVariables;
    }

    private QueryRequest buildQueryRequest(String caseId) {
        return QueryRequest.builder()
            .roleType(Collections.singletonList(RoleType.CASE))
            .roleName(Collections.singletonList(RoleName.TRIBUNAL_CASEWORKER))
            .validAt(LocalDateTime.now())
            .attributes(Collections.singletonMap(Attributes.CASE_ID, Collections.singletonList(caseId)))
            .build();
    }
}
