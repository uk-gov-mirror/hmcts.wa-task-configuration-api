package uk.gov.hmcts.reform.wataskconfigurationapi.exceptions;

public class ServerErrorException extends RuntimeException {

    private static final long serialVersionUID = -136972100853598890L;

    public ServerErrorException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
