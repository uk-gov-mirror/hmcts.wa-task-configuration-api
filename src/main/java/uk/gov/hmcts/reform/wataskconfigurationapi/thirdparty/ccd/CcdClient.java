package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
    name = "ccd-client",
    url = "${ccd.url}"
)
public interface CcdClient {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String EXPERIMENTAL = "experimental=true";

    @GetMapping(
        path = "/cases/{cid}",
        headers = EXPERIMENTAL
    )
    String getCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("cid") String caseId
    );

}
