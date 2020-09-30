package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {
    private String jurisdiction;
    @JsonProperty("case_type_id")
    @JsonAlias("case_type")
    private String caseTypeId;
    @JsonProperty("security_classification")
    private String securityClassification;

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public String getSecurityClassification() {
        return securityClassification;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        CaseDetails that = (CaseDetails) object;
        return Objects.equals(jurisdiction, that.jurisdiction)
               && Objects.equals(caseTypeId, that.caseTypeId)
               && Objects.equals(securityClassification, that.securityClassification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jurisdiction, caseTypeId);
    }

    @Override
    public String toString() {
        return "CaseDetails{"
               + "jurisdiction='" + jurisdiction + '\''
               + ", caseTypeId='" + caseTypeId + '\''
               + ", securityClassification='" + securityClassification + '\''
               + '}';
    }
}
