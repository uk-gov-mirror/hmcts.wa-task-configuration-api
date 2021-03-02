package uk.gov.hmcts.reform.wataskconfigurationapi.config;


import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.UserDetails;

public interface UserDetailsProvider {

    UserDetails getUserDetails();

}
