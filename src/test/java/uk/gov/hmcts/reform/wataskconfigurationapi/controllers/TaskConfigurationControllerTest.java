package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.request.ConfigureTaskRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.response.ConfigureTaskResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.ConfigureTaskService;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
class TaskConfigurationControllerTest {

    @Mock
    private ConfigureTaskService configureTaskService;

    private String caseId = UUID.randomUUID().toString();
    private String taskName;

    private TaskConfigurationController taskConfigurationController;

    @BeforeEach
    void setUp() {

        taskConfigurationController = new TaskConfigurationController(configureTaskService);

        caseId = UUID.randomUUID().toString();

        taskName = "processApplication";
    }

    @Test
    void should_configure_task() {

        final String taskId = UUID.randomUUID().toString();

        final ResponseEntity<String> response = taskConfigurationController.configureTask(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void should_get_configuration_for_task() {

        final String taskId = UUID.randomUUID().toString();

        final ConfigureTaskRequest configureTaskRequest = getConfigureTaskRequest();

        final ResponseEntity<ConfigureTaskResponse> response =
            taskConfigurationController.getConfigurationForTask(
                taskId,
                configureTaskRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private ConfigureTaskRequest getConfigureTaskRequest() {

        return new ConfigureTaskRequest(caseId, taskName, Collections.emptyMap());
    }
}
