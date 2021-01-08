package uk.gov.hmcts.reform.wataskconfigurationapi.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 3739028681183411142L;

    public ResourceNotFoundException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
