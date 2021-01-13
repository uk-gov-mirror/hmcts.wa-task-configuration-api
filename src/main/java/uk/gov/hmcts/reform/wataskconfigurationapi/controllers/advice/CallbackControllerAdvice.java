package uk.gov.hmcts.reform.wataskconfigurationapi.controllers.advice;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.ResourceNotFoundException;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.wataskconfigurationapi.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class CallbackControllerAdvice extends ResponseEntityExceptionHandler {
    public static final String EXCEPTION_OCCURRED = "Exception occurred: {}";

    private static final Logger LOG = getLogger(CallbackControllerAdvice.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<ErrorMessage> handleResourceNotFoundException(
        Exception ex
    ) {
        LOG.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorMessage(
                    ex,
                    HttpStatus.NOT_FOUND,
                    Timestamp.valueOf(LocalDateTime.now())
                )
            );
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorMessage> handleGenericException(
        Exception ex
    ) {
        LOG.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorMessage(
                    ex,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    Timestamp.valueOf(LocalDateTime.now())
                )
            );
    }


}
