package uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.UserInfo;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.IdamServiceApi;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdamServiceTest {

    @Mock
    private IdamServiceApi idamServiceApi;

    private UserInfo userInfo;
    private String accessToken;

    private IdamService idamService;

    @BeforeEach
    void setUp() {

        userInfo = mock((UserInfo.class));
        accessToken = "accessToken";
        idamService = new IdamService(idamServiceApi);

        when(idamServiceApi.userInfo(accessToken)).thenReturn(userInfo);
    }

    @Test
    void should_get_user_info() {

        final UserInfo actualUserInfo = idamService.getUserInfo(accessToken);
        assertNotNull(actualUserInfo);
        assertEquals(userInfo, actualUserInfo);
    }

    @Test
    void should_get_user_id() {

        final String expectedUserId = UUID.randomUUID().toString();
        when(userInfo.getUid()).thenReturn(expectedUserId);

        final String user_Id = idamService.getUserId(accessToken);
        assertNotNull(user_Id);
        assertEquals(expectedUserId, user_Id);
    }
}
