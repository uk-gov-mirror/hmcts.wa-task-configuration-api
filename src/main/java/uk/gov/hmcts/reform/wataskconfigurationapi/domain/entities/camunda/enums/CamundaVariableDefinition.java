package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums;

public enum CamundaVariableDefinition {

    APPEAL_TYPE("appealType"),
    AUTO_ASSIGNED("autoAssigned"),
    CASE_NAME("caseName"),
    CASE_TYPE_ID("caseTypeId"),
    EXECUTION_TYPE("executionType"),
    LOCATION("location"),
    LOCATION_NAME("locationName"),
    REGION("region"),
    SECURITY_CLASSIFICATION("securityClassification"),
    TASK_STATE("taskState"),
    TASK_SYSTEM("taskSystem"),
    TITLE("title"),
    JURISDICTION("jurisdiction"),
    ASSIGNEE("assignee"),
    TASK_ID("taskId"),
    TYPE("type"),
    FORM_KEY("formKey"),
    CASE_ID("caseId");

    private final String value;

    CamundaVariableDefinition(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
