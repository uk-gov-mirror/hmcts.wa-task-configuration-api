package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CcdDataService {
    //todo look this up in CCD
    @SuppressWarnings({
        "PMD.UselessParentheses",
        "PMD.AvoidThrowingRawExceptionTypes"
    })
    public String getCaseData(String ccdId) {
        try {
            return new String((Thread.currentThread().getContextClassLoader().getResourceAsStream("case_data.json"))
                                  .readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
