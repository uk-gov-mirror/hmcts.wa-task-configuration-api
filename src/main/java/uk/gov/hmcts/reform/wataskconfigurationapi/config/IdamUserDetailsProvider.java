package uk.gov.hmcts.reform.wataskconfigurationapi.config;

import feign.FeignException;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.UserInfo;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.IdamServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.IdamUserDetails;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.IdentityManagerResponseException;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis","PMD.CyclomaticComplexity"})
public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final IdamServiceApi idamServiceApi;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        IdamServiceApi idamServiceApi
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.idamServiceApi = idamServiceApi;
    }

    @Override
    public IdamUserDetails getUserDetails() {

        String accessToken = accessTokenProvider.getAccessToken();

        UserInfo response;

        try {
            response = idamServiceApi.userInfo(accessToken);

        } catch (FeignException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.getUid() == null) {
            throw new IllegalStateException("IDAM user details missing 'uid' field");
        }

        if (response.getRoles() == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        if (response.getEmail() == null) {
            throw new IllegalStateException("IDAM user details missing 'sub' field");
        }

        if (response.getGivenName() == null) {
            throw new IllegalStateException("IDAM user details missing 'given_name' field");
        }

        if (response.getFamilyName() == null) {
            throw new IllegalStateException("IDAM user details missing 'family_name' field");
        }

        return new IdamUserDetails(
            accessToken,
            response.getUid(),
            response.getRoles(),
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }
}
