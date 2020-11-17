package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class IdamSystemTokenGenerator {

    private final String systemUserName;
    private final String systemUserPass;
    private final String idamRedirectUrl;
    private final String systemUserScope;
    private final String idamClientId;
    private final String idamClientSecret;
    private final IdamApi idamApi;

    public IdamSystemTokenGenerator(
        @Value("${idam.system.username}") String systemUserName,
        @Value("${idam.system.password}") String systemUserPass,
        @Value("${idam.redirectUrl}") String idamRedirectUrl,
        @Value("${idam.system.scope}") String systemUserScope,
        @Value("${spring.security.oauth2.client.registration.oidc.client-id}") String idamClientId,
        @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") String idamClientSecret,
        IdamApi idamApi
    ) {
        this.systemUserName = systemUserName;
        this.systemUserPass = systemUserPass;
        this.idamRedirectUrl = idamRedirectUrl;
        this.systemUserScope = systemUserScope;
        this.idamClientId = idamClientId;
        this.idamClientSecret = idamClientSecret;
        this.idamApi = idamApi;
    }

    public String generate() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", idamRedirectUrl);
        map.add("client_id", idamClientId);
        map.add("client_secret", idamClientSecret);
        map.add("username", systemUserName);
        map.add("password", systemUserPass);
        map.add("scope", systemUserScope);
        Token tokenResponse = idamApi.token(map);

        return "Bearer " + tokenResponse.getAccessToken();
    }

    public UserInfo getUserInfo(String bearerAccessToken) {
        return idamApi.userInfo(bearerAccessToken);
    }
}
