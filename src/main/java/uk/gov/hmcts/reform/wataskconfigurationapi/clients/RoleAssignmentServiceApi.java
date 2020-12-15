package uk.gov.hmcts.reform.wataskconfigurationapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.QueryRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment.RoleAssignmentResource;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.wataskconfigurationapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "role-assignment-service-api",
    url = "${role-assignment-service.url}"
)
public interface RoleAssignmentServiceApi {

    @PostMapping(value = "/am/role-assignments/query", consumes = "application/json")
    RoleAssignmentResource queryRoleAssignments(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @RequestBody QueryRequest queryRequest
    );

}
