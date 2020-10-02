package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TaskResponse {
    private String id;
    @JsonProperty("processInstanceId")
    private String processInstanceId;
    private String name;

    private TaskResponse() {
    }

    public TaskResponse(String id, String processInstanceId, String name) {
        this.id = id;
        this.processInstanceId = processInstanceId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getName() {
        return name;
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
               && Objects.equals(processInstanceId, that.processInstanceId)
               && Objects.equals(name, that.name);
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
               + ", name='" + name + '\''
               + '}';
    }
}
