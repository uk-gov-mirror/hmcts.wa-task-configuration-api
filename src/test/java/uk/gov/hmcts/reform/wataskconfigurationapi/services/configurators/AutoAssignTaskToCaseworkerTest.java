package uk.gov.hmcts.reform.wataskconfigurationapi.services.configurators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignmentResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AutoAssignTaskToCaseworkerTest {

    public static final String USER_TOKEN = "user token";
    public static final String S_2_S_TOKEN = "s2s token";
    @Mock
    private RoleAssignmentServiceApi roleAssignmentServiceApi;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private IdamSystemTokenGenerator idamSystemTokenGenerator;

    @InjectMocks
    private AutoAssignTaskToCaseworker autoAssignTaskToCaseworker;


    @Test
    void getConfigurationVariables() {

        Mockito.when(idamSystemTokenGenerator.generate()).thenReturn(USER_TOKEN);
        Mockito.when(serviceAuthTokenGenerator.generate()).thenReturn(S_2_S_TOKEN);

        RoleAssignmentResource roleAssignmentResource = new RoleAssignmentResource(
            List.of(),
            null
        );

        Mockito.when(roleAssignmentServiceApi.queryRoleAssignments(
            eq(USER_TOKEN),
            eq(S_2_S_TOKEN),
            any(QueryRequest.class)
        )).thenReturn(roleAssignmentResource);

        CamundaTask task = new CamundaTask(
            "some id",
            "some process id",
            "some name"
        );

        HashMap<String, CamundaValue<Object>> processVariables = new HashMap<>();
        processVariables.put("caseId", new CamundaValue<>("some case id value", "String"));

        Map<String, Object> actualResult = autoAssignTaskToCaseworker.getConfigurationVariables(
            task,
            processVariables
        );

        assertThat(actualResult.get("taskState")).isEqualTo("unassigned");

    }
}
