package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AssigneeRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.TaskState;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.TaskResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.roleassignment.RoleAssignmentClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order
public class AutoAssignTaskToCaseworker implements TaskVariableExtractor {

    private final RoleAssignmentClient roleAssignmentClient;
    private final CamundaClient camundaClient;
    private final AuthTokenGenerator ccdServiceAuthTokenGenerator;
    private final AuthTokenGenerator camundaServiceAuthTokenGenerator;
    private final IdamSystemTokenGenerator idamSystemTokenGenerator;

    public AutoAssignTaskToCaseworker(RoleAssignmentClient roleAssignmentClient,
                                      CamundaClient camundaClient,
                                      @Qualifier("ccdServiceAuthTokenGenerator")
                                          AuthTokenGenerator ccdServiceAuthTokenGenerator,
                                      @Qualifier("camundaServiceAuthTokenGenerator")
                                          AuthTokenGenerator camundaServiceAuthTokenGenerator,
                                      IdamSystemTokenGenerator idamSystemTokenGenerator) {
        this.roleAssignmentClient = roleAssignmentClient;
        this.camundaClient = camundaClient;
        this.ccdServiceAuthTokenGenerator = ccdServiceAuthTokenGenerator;
        this.camundaServiceAuthTokenGenerator = camundaServiceAuthTokenGenerator;
        this.idamSystemTokenGenerator = idamSystemTokenGenerator;
    }

    @Override
    public Map<String, Object> getValues(TaskResponse task, Map<String, CamundaValue<Object>> processVariables) {
        String ccdId = (String) processVariables.get(ConfigureTaskService.CCD_ID_PROCESS_VARIABLE_KEY).getValue();

        List<RoleAssignment> roleAssignmentList = roleAssignmentClient.queryRoleAssignments(
            idamSystemTokenGenerator.generate(),
            ccdServiceAuthTokenGenerator.generate(),
            buildQueryRequest(ccdId)
        );

        return updateTaskStateAndSetAssignee(task, roleAssignmentList);
    }

    @SuppressWarnings({"PMD.LawOfDemeter"})
    private Map<String, Object> updateTaskStateAndSetAssignee(TaskResponse task,
                                                              List<RoleAssignment> roleAssignmentList) {
        Map<String, Object> taskVariables = new ConcurrentHashMap<>();
        if (roleAssignmentList.isEmpty()) {
            taskVariables.put("taskState", TaskState.UNASSIGNED.getValue());
        } else {
            String actorId = roleAssignmentList.get(0).getActorId();
            camundaClient.setAssignee(
                camundaServiceAuthTokenGenerator.generate(),
                task.getId(),
                new AssigneeRequest(actorId)
            );
            taskVariables.put("taskState", TaskState.ASSIGNED.getValue());
        }
        return taskVariables;
    }

    private QueryRequest buildQueryRequest(String ccdId) {
        return QueryRequest.builder()
            .roleType(Collections.singletonList(RoleType.CASE))
            .roleName(Collections.singletonList(RoleName.TRIBUNAL_CASEWORKER))
            .validAt(LocalDateTime.now())
            .attributes(Collections.singletonMap(Attributes.CASE_ID, Collections.singletonList(ccdId)))
            .build();
    }
}
