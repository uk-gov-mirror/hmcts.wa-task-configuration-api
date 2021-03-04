package uk.gov.hmcts.reform.wataskconfigurationapi;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.wataskconfigurationapi.util.LaunchDarklyFunctionalTestClient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LaunchDarklyFunctionalTest extends SpringBootFunctionalBaseTest {


    @Value("${launchDarkly.sdkKey}")
    private String sdkKey;

    @Autowired
    private LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient;


    @Test
    public void should_hit_launch_darkly() {
        boolean launchDarklyFeature = launchDarklyFunctionalTestClient.getKey(sdkKey);

        assertThat(launchDarklyFeature, is(false));
    }
}
