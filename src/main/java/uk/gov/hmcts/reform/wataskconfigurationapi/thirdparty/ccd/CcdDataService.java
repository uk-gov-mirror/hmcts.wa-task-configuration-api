package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.IdamSystemTokenGenerator;

@Component
public class CcdDataService {
    private final CcdClient ccdClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamSystemTokenGenerator systemTokenGenerator;

    public CcdDataService(
        CcdClient ccdClient,
        AuthTokenGenerator authTokenGenerator,
        IdamSystemTokenGenerator systemTokenGenerator
    ) {
        this.ccdClient = ccdClient;
        this.authTokenGenerator = authTokenGenerator;
        this.systemTokenGenerator = systemTokenGenerator;
    }

    public String getCaseData(String ccdId) {
        return ccdClient.getCase(
            "Bearer " + systemTokenGenerator.generate(),
            authTokenGenerator.generate(),
            ccdId
        );
    }
}
