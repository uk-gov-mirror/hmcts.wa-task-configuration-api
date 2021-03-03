package uk.gov.hmcts.reform.wataskconfigurationapi;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.wataskconfigurationapi.util.LaunchDarklyFunctionalTestClient;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.is;

public class LaunchDarklyFunctionalTest  extends SpringBootFunctionalBaseTest{

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient;


    @Test
    public void should_hit_launch_darkly() {
        String accessToken = authorizationHeadersProvider.getServiceAuthorizationHeader().getValue();
        boolean launchDarklyFeature = launchDarklyFunctionalTestClient.getKey("", accessToken);

        assertThat(launchDarklyFeature, is(true));
    }
}
