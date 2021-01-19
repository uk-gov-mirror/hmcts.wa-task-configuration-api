package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class DecisionTableRequest {

    private CamundaValue<String> caseJson;

    private DecisionTableRequest() {
    }

    public DecisionTableRequest(CamundaValue<String> caseJson) {
        this.caseJson = caseJson;
    }

    @JsonProperty("case")
    public CamundaValue<String> getCaseJson() {
        return caseJson;
    }

}
