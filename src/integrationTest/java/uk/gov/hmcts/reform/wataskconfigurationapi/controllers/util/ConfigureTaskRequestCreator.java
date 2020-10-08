package uk.gov.hmcts.reform.wataskconfigurationapi.controllers.util;

import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.ConfigureTaskRequest;

import java.util.UUID;

public class ConfigureTaskRequestCreator {
    private String taskId;

    public ConfigureTaskRequestCreator withTaskId(String taskId) {
        this.taskId = taskId;

        return this;
    }

    public static ConfigureTaskRequestCreator createConfigureTaskRequest() {
        return new ConfigureTaskRequestCreator()
            .withTaskId(UUID.randomUUID().toString());
    }

    public ConfigureTaskRequest build() {
        return new ConfigureTaskRequest(taskId);
    }

    public String asString() {
        return CreatorObjectMapper.asJsonString(build());
    }
}
