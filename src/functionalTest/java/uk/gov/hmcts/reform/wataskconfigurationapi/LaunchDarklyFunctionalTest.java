package uk.gov.hmcts.reform.wataskconfigurationapi;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.wataskconfigurationapi.util.LaunchDarklyFunctionalTestClient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LaunchDarklyFunctionalTest extends SpringBootFunctionalBaseTest {

    private final LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient;

    @Autowired
    public LaunchDarklyFunctionalTest(LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient) {
        this.launchDarklyFunctionalTestClient = launchDarklyFunctionalTestClient;
    }

    @Test
    public void should_hit_launch_darkly() {
        boolean launchDarklyFeature = launchDarklyFunctionalTestClient.getKey("tester");

        assertThat(launchDarklyFeature, is(true));
    }
}
