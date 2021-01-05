package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration;

public class AutoAssignmentResult {
    private final String taskState;
    private final String assignee;

    public AutoAssignmentResult(String taskState, String assignee) {
        this.taskState = taskState;
        this.assignee = assignee;
    }

    public String getTaskState() {
        return taskState;
    }

    public String getAssignee() {
        return assignee;
    }
}
