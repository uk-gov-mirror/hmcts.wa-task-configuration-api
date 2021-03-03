package uk.gov.hmcts.reform.wataskconfigurationapi.util;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.UserInfo;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.IdamServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.IdamUserDetails;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.IdentityManagerResponseException;

@Component
public class LaunchDarklyFunctionalTestClient {

    @Autowired
    private IdamServiceApi idamServiceApi;

    @Autowired
    private LDClientInterface ldClient;

    public boolean getKey(String key, String accessToken) {

        UserDetails userDetails = getUserDetails(accessToken);

        LDUser ldUser =  new LDUser.Builder(userDetails.getId())
            .firstName(userDetails.getForename())
            .lastName(userDetails.getSurname())
            .email(userDetails.getEmailAddress())
            .build();

        return ldClient.boolVariation(key, ldUser, false);
    }

    private IdamUserDetails getUserDetails(String accessToken) {
        try {

            UserInfo userInfo = idamServiceApi.userInfo(accessToken);

            return new IdamUserDetails(
                accessToken,
                userInfo.getUid(),
                userInfo.getRoles(),
                userInfo.getEmail(),
                userInfo.getGivenName(),
                userInfo.getFamilyName()
            );

        } catch (RestClientResponseException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }
    }
}
