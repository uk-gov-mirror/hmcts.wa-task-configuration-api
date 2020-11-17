package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.IdamSystemTokenGenerator;

@Component
public class CcdDataService {
    private final CcdClient ccdClient;
    private final AuthTokenGenerator ccdServiceAuthTokenGenerator;
    private final IdamSystemTokenGenerator systemTokenGenerator;

    public CcdDataService(
        CcdClient ccdClient,
        @Qualifier("ccdServiceAuthTokenGenerator") AuthTokenGenerator ccdServiceAuthTokenGenerator,
        IdamSystemTokenGenerator systemTokenGenerator
    ) {
        this.ccdClient = ccdClient;
        this.ccdServiceAuthTokenGenerator = ccdServiceAuthTokenGenerator;
        this.systemTokenGenerator = systemTokenGenerator;
    }

    public String getCaseData(String ccdId) {
        return ccdClient.getCase(
            systemTokenGenerator.generate(),
            ccdServiceAuthTokenGenerator.generate(),
            ccdId
        );
    }
}
