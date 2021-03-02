package uk.gov.hmcts.reform.wataskconfigurationapi.config;

import java.util.Optional;

public interface AccessTokenProvider {

    String getAccessToken();

    Optional<String> tryGetAccessToken();
}
