package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleName {
    TRIBUNAL_CASEWORKER("tribunal-caseworker"),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    RoleName(String value) {
        this.value = value;
    }
}
