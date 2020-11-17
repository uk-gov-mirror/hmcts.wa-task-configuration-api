package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@JsonNaming
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class AssigneeRequest {
    private final String userId;

    public AssigneeRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
