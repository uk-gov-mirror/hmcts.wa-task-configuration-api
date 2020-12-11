package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.IdamSystemTokenGenerator;

@Component
public class CcdDataService {
    private final CcdClient ccdClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamSystemTokenGenerator systemTokenGenerator;

    public CcdDataService(
        CcdClient ccdClient,
        AuthTokenGenerator serviceAuthTokenGenerator,
        IdamSystemTokenGenerator systemTokenGenerator
    ) {
        this.ccdClient = ccdClient;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.systemTokenGenerator = systemTokenGenerator;
    }

    public String getCaseData(String caseId) {
        return ccdClient.getCase(
            systemTokenGenerator.generate(),
            serviceAuthTokenGenerator.generate(),
            caseId
        );
    }
}
