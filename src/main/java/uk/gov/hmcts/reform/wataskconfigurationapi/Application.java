package uk.gov.hmcts.reform.wataskconfigurationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCircuitBreaker
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping",
        "uk.gov.hmcts.reform.wataskconfigurationapi.idam",
        "uk.gov.hmcts.reform.wataskconfigurationapi.ccd",
        "uk.gov.hmcts.reform.ccd.client"
    })
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
