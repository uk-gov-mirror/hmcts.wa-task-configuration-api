package uk.gov.hmcts.reform.wataskconfigurationapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.Token;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.UserInfo;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "idam-api",
    url = "${idam.baseUrl}"
)
public interface IdamServiceApi {
    @GetMapping(value = "/o/userinfo", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    UserInfo userInfo(@RequestHeader(AUTHORIZATION) String userToken);

    @PostMapping(value = "/o/token", produces = APPLICATION_JSON_VALUE,
        consumes = "application/x-www-form-urlencoded")
    Token token(@RequestBody Map<String, ?> form);
}
