package uk.gov.hmcts.reform.wataskconfigurationapi.controllers;

import java.util.Objects;

public class ConfigureTaskRequest {
    private String taskId;

    private ConfigureTaskRequest() {
    }

    public ConfigureTaskRequest(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ConfigureTaskRequest that = (ConfigureTaskRequest) object;
        return Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "ConfigureTaskRequest{"
               + "taskId='" + taskId + '\''
               + '}';
    }
}
