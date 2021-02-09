package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.request.ConfigureTaskRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.response.ConfigureTaskResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration.TaskToConfigure;
import uk.gov.hmcts.reform.wataskconfigurationapi.exceptions.ConfigureTaskException;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.ConfigureTaskService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.NAME;

@Slf4j
@RequestMapping(
    path = "/task",
    consumes = APPLICATION_JSON_VALUE,
    produces = APPLICATION_JSON_VALUE
)
@RestController
public class TaskConfigurationController {
    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String OK = "OK";
    private static final String BAD_REQUEST = "Bad Request";
    private static final String FORBIDDEN = "Forbidden";
    private static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String TASK_ID = "task-id";
    private final ConfigureTaskService configureTaskService;

    public TaskConfigurationController(ConfigureTaskService configureTaskService) {
        this.configureTaskService = configureTaskService;
    }


    @ApiOperation("Given an existent task id configures a task over rest")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = OK
        ),
        @ApiResponse(
            code = 400,
            message = BAD_REQUEST
        ),
        @ApiResponse(
            code = 403,
            message = FORBIDDEN
        ),
        @ApiResponse(
            code = 401,
            message = UNAUTHORIZED
        ),
        @ApiResponse(
            code = 415,
            message = UNSUPPORTED_MEDIA_TYPE
        ),
        @ApiResponse(
            code = 500,
            message = INTERNAL_SERVER_ERROR
        )
    })
    @PostMapping(
        path = "/{task-id}",
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> configureTask(
        @PathVariable(TASK_ID) String taskId) {
        try {
            configureTaskService.configureTask(taskId);
            return ResponseEntity
                .ok()
                .body("OK");
        } catch (ConfigureTaskException exc) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiOperation("Retrieves a list of configuration variables to be added to a task")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = OK,
            response = ConfigureTaskResponse.class
        ),
        @ApiResponse(
            code = 400,
            message = BAD_REQUEST
        ),
        @ApiResponse(
            code = 403,
            message = FORBIDDEN
        ),
        @ApiResponse(
            code = 401,
            message = UNAUTHORIZED
        ),
        @ApiResponse(
            code = 415,
            message = UNSUPPORTED_MEDIA_TYPE
        ),
        @ApiResponse(
            code = 500,
            message = INTERNAL_SERVER_ERROR
        )
    })
    @PostMapping(
        path = "/{task-id}/configuration",
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConfigureTaskResponse> getConfigurationForTask(
        @PathVariable(TASK_ID) String taskId,
        @RequestBody ConfigureTaskRequest configureTaskRequest) {
        log.info("Received call to configure task: {}", taskId);
        Map<String, Object> variables = configureTaskRequest.getProcessVariables();
        log.info("{}", variables);

        String caseId = (String) variables.get(CASE_ID.value());
        String taskName = (String) variables.get(NAME.value());

        ConfigureTaskResponse response =
            configureTaskService.getConfiguration(
                new TaskToConfigure(
                    taskId,
                    caseId,
                    taskName,
                    configureTaskRequest.getProcessVariables()
                )
            );

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .body(response);
    }

}
