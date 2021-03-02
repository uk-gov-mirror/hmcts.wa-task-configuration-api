package uk.gov.hmcts.reform.wataskconfigurationapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
