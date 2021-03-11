package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.roleassignment;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleAssignment;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleAttributeDefinition;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums.ActorIdType;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums.Classification;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums.GrantType;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums.RoleCategory;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums.RoleType;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.response.RoleAssignmentResource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@JsonTest
class RoleAssignmentTest {

    @Autowired
    private JacksonTester<RoleAssignmentResource> jacksonTester;

    @Test
    public void testDeserializeRoleAssignment() throws Exception {
        RoleAssignment expectedRoleAssignment = RoleAssignment.builder()
            .id("4c704d91-de05-43e1-a9ee-9b8dc87c2c12")
            .actorIdType(ActorIdType.IDAM)
            .actorId("4afa7d5c-02fa-4a82-82c2-0a9ad7467d30")
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .classification(Classification.RESTRICTED)
            .grantType(GrantType.SPECIFIC)
            .roleCategory(RoleCategory.LEGAL_OPERATIONS)
            .readOnly(false)
            .created(LocalDateTime.parse("2020-11-06T17:15:36.960886"))
            .attributes(Map.of(
                RoleAttributeDefinition.CASE_ID.value(), "1604584759556245",
                RoleAttributeDefinition.JURISDICTION.value(), "IA",
                RoleAttributeDefinition.CASE_TYPE.value(), "Asylum"
                ))
            .authorisations(Collections.emptyList())
            .build();

        ObjectContent<RoleAssignmentResource> actualRoleAssignment =
            jacksonTester.read("roleAssignment.json");

        assertThat(actualRoleAssignment.getObject()).isNotNull();
        assertThat(actualRoleAssignment.getObject().getRoleAssignmentResponse())
            .isEqualTo(asList(expectedRoleAssignment));
    }
}
