package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Attributes {
    CASE_ID("caseId");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    Attributes(String value) {
        this.value = value;
    }
}
