package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskException;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.ConfigureTaskService;

@RestController
public class ConfigurationController {
    private final ConfigureTaskService configureTaskService;

    public ConfigurationController(ConfigureTaskService configureTaskService) {
        this.configureTaskService = configureTaskService;
    }

    @PostMapping(
        path = "/configureTask",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> configureTask(@RequestBody ConfigureTaskRequest configureTaskRequest) {
        try {
            configureTaskService.configureTask(configureTaskRequest.getTaskId());
            return ResponseEntity
                .ok()
                .body("OK");
        } catch (ConfigureTaskException exc) {
            return ResponseEntity.notFound().build();
        }
    }
}
