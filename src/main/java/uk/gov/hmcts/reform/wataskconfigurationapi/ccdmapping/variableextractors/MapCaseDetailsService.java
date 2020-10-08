package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.MapCaseDataDmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.MapCaseDataDmnResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CaseDetails;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CcdDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;

@Component
public class MapCaseDetailsService {
    private final CcdDataService ccdDataService;
    private final CamundaClient camundaClient;

    public MapCaseDetailsService(CcdDataService ccdDataService, CamundaClient camundaClient) {
        this.ccdDataService = ccdDataService;
        this.camundaClient = camundaClient;
    }

    public Map<String, Object> getMappedDetails(String ccdId) {
        String caseData = ccdDataService.getCaseData(ccdId);

        try {
            CaseDetails caseDetails = new ObjectMapper().readValue(caseData, CaseDetails.class);
            List<MapCaseDataDmnResult> mapCaseDataDmnResults = camundaClient.mapCaseData(
                caseDetails.getJurisdiction(),
                caseDetails.getCaseTypeId(),
                new DmnRequest<>(
                    new MapCaseDataDmnRequest(jsonValue(caseData))
                )
            );

            Map<String, Object> mappedCaseDetails = mapCaseDataDmnResults.stream().collect(toMap(
                mapCaseDataDmnResult -> mapCaseDataDmnResult.getName().getValue(),
                mapCaseDataDmnResult -> mapCaseDataDmnResult.getValue().getValue()
            ));

            HashMap<String, Object> allMappedDetails = new HashMap<>(mappedCaseDetails);
            allMappedDetails.put("securityClassification", caseDetails.getSecurityClassification());
            allMappedDetails.put("caseType", caseDetails.getCaseTypeId());
            return allMappedDetails;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse result from CCD for [" + ccdId + "]", e);
        }
    }
}
