package uk.gov.hmcts.reform.wataskconfigurationapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
public class ServiceTokenGeneratorConfiguration {

    @Bean(name = "ccdServiceAuthTokenGenerator")
    public AuthTokenGenerator ccdServiceAuthTokenGenerator(
        @Value("${idam.s2s-auth.secret.ccd}") String secret,
        @Value("${idam.s2s-auth.name.ccd}") String microService,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean(name = "camundaServiceAuthTokenGenerator")
    public AuthTokenGenerator camundaServiceAuthTokenGenerator(
        @Value("${idam.s2s-auth.secret.camunda}") String secret,
        @Value("${idam.s2s-auth.name.camunda}") String microService,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }
}
