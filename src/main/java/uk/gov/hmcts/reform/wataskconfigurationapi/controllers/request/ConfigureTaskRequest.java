package uk.gov.hmcts.reform.wataskconfigurationapi.controllers.request;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode
@ToString
public class ConfigureTaskRequest {
    private String caseId;
    private String taskName;
    private Map<String, Object> processVariables;

    private ConfigureTaskRequest() {
        //No-op constructor for deserialization
    }

    public ConfigureTaskRequest(String caseId, String taskName, Map<String, Object> processVariables) {
        this.caseId = caseId;
        this.taskName = taskName;
        this.processVariables = processVariables;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTaskName() {
        return taskName;
    }

    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }
}
