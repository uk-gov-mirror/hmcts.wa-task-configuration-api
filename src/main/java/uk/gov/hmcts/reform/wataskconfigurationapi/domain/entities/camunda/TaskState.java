package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskState {
    UNCONFIGURED("unconfigured"),
    UNASSIGNED("unassigned"),
    CONFIGURED("configured"),
    ASSIGNED("assigned"),
    REFERRED("referred"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    TaskState(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
