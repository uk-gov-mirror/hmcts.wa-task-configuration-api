package uk.gov.hmcts.reform.wataskconfigurationapi.exceptions;

public class IdentityManagerResponseException extends RuntimeException {

    public IdentityManagerResponseException(
        String message,
        Throwable cause) {

        super(message, cause);

    }

}
