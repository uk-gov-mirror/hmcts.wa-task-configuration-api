package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TaskResponse {
    private String id;
    @JsonProperty("processInstanceId")
    private String processInstanceId;

    private TaskResponse() {
    }

    public TaskResponse(String id, String processInstanceId) {
        this.id = id;
        this.processInstanceId = processInstanceId;
    }

    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        TaskResponse that = (TaskResponse) object;
        return Objects.equals(id, that.id)
               && Objects.equals(processInstanceId, that.processInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, processInstanceId);
    }

    @Override
    public String toString() {
        return "TaskResponse{"
               + "id='" + id + '\''
               + ", processInstanceId='" + processInstanceId + '\''
               + '}';
    }
}
