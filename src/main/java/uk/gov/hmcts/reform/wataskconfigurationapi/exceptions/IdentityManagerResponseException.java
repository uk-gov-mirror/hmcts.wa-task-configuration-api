package uk.gov.hmcts.reform.wataskconfigurationapi.exceptions;

public class IdentityManagerResponseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IdentityManagerResponseException(
        String message,
        Throwable cause) {

        super(message, cause);

    }

}
