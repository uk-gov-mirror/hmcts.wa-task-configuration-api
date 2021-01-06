package uk.gov.hmcts.reform.wataskconfigurationapi.controllers.util;

import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.request.ConfigureTaskRequest;

import java.util.UUID;

import static java.util.Collections.emptyMap;

public class ConfigureTaskRequestCreator {
    private String taskId;

    public static ConfigureTaskRequestCreator createConfigureTaskRequest() {
        return new ConfigureTaskRequestCreator()
            .withTaskId(UUID.randomUUID().toString());
    }

    public ConfigureTaskRequestCreator withTaskId(String taskId) {
        this.taskId = taskId;

        return this;
    }

    public ConfigureTaskRequest build() {
        return new ConfigureTaskRequest(taskId, "a task name", emptyMap());
    }

    public String asString() {
        return CreatorObjectMapper.asJsonString(build());
    }
}
