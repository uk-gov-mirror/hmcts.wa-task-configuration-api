package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.RoleAssignmentServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.Attributes;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.Classification;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.GrantType;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignment;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
public class RoleAssignmentServiceApiTest {

    private static WireMockServer wireMockServer;
    @Autowired
    private RoleAssignmentServiceApi roleAssignmentServiceApi;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    void queryRoleAssignmentTest() throws IOException {

        String roleAssignmentsResponseAsJsonString = loadJsonFileResource();

        stubRoleAssignmentApiResponse(roleAssignmentsResponseAsJsonString);

        RoleAssignmentResource roleAssignmentResource = roleAssignmentServiceApi.queryRoleAssignments(
            "user token",
            "s2s token",
            QueryRequest.builder().build()
        );

        RoleAssignment expectedRoleAssignment = RoleAssignment.builder()
            .id("428971b1-3954-4783-840f-c2718732b466")
            .actorIdType(ActorIdType.IDAM)
            .actorId("122f8de4-2eb6-4dcf-91c9-16c2c8aaa422")
            .roleType(RoleType.CASE)
            .roleName(RoleName.TRIBUNAL_CASEWORKER)
            .classification(Classification.RESTRICTED)
            .grantType(GrantType.SPECIFIC)
            .roleCategory(RoleCategory.STAFF)
            .readOnly(false)
            .created(LocalDateTime.parse("2020-11-09T14:32:23.693195"))
            .attributes(Map.of(Attributes.CASE_ID, "1604929600826893"))
            .authorisations(Collections.emptyList())
            .build();

        assertThat(roleAssignmentResource.getRoleAssignmentResponse()).isNotEmpty();
        assertThat(roleAssignmentResource.getRoleAssignmentResponse().get(0)).isEqualTo(expectedRoleAssignment);
    }

    private void stubRoleAssignmentApiResponse(String roleAssignmentsResponseAsJsonString) {
        wireMockServer.stubFor(post(urlEqualTo("/am/role-assignments/query")).willReturn(
            aResponse()
                .withStatus(200)
                .withHeader(
                    "Content-Type",
                    "application/vnd.uk.gov.hmcts.role-assignment-service.post-assignment-query-request+json; "
                    + "version=1.0;charset=UTF-8"
                )
                .withBody(roleAssignmentsResponseAsJsonString))
        );
    }

    private String loadJsonFileResource() throws IOException {
        return FileUtils.readFileToString(ResourceUtils.getFile(
            "classpath:uk/gov/hmcts/reform/wataskconfigurationapi/ccdmapping/variableextractors/"
            + "roleAssignmentsResponse.json"));
    }

}
