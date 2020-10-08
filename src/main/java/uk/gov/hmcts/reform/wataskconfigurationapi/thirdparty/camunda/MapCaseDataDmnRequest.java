package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MapCaseDataDmnRequest {

    private final CamundaValue<String> caseJson;

    public MapCaseDataDmnRequest(CamundaValue<String> caseJson) {
        this.caseJson = caseJson;
    }

    @JsonProperty("case")
    public CamundaValue<String> getCaseJson() {
        return caseJson;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MapCaseDataDmnRequest that = (MapCaseDataDmnRequest) object;
        return Objects.equals(caseJson, that.caseJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseJson);
    }

    @Override
    public String toString() {
        return "MapCaseDataDmnRequest{"
               + "caseJson=" + caseJson
               + '}';
    }
}
