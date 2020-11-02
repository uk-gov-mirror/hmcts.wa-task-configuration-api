package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdamSystemTokenGeneratorTest {

    final String idamRedirectUrl = "idamRedirectUrl";
    final String idamClientId = "idamClientId";
    final String idamClientSecret = "idamClientSecret";
    final String systemUserName = "systemUserName";
    final String systemUserPass = "systemUserPass";
    final String systemUserScope = "systemUserScope";

    @Mock
    IdamApi idamApi;

    @Mock
    Token token;

    @Mock
    UserInfo userInfo;

    private IdamSystemTokenGenerator idamSystemTokenGenerator;

    @Before
    public void setUp() {
        idamSystemTokenGenerator = new IdamSystemTokenGenerator(
            systemUserName,
            systemUserPass,
            idamRedirectUrl,
            systemUserScope,
            idamClientId,
            idamClientSecret,
            idamApi
        );
    }

    @Test
    public void should_generate() {
        final String returnToken = "returnToken";

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", idamRedirectUrl);
        map.add("client_id", idamClientId);
        map.add("client_secret", idamClientSecret);
        map.add("username", systemUserName);
        map.add("password", systemUserPass);
        map.add("scope", systemUserScope);

        when(idamApi.token(map)).thenReturn(token);
        when(token.getAccessToken()).thenReturn(returnToken);

        final String actualToken = idamSystemTokenGenerator.generate();

        assertEquals(actualToken, returnToken);
    }

    @Test
    public void getUserInfo() {
        final String accessToken = "accessToken";
        when(idamApi.userInfo(accessToken)).thenReturn(userInfo);

        final UserInfo actualUserInfo = idamSystemTokenGenerator.getUserInfo(accessToken);

        assertEquals(actualUserInfo, userInfo);
    }
}