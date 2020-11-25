package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CaseDetails;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CcdDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;

@Component
public class MapCaseDetailsService {
    public static final String MAP_CASE_DATA_DECISION_TABLE_NAME = "mapCaseData";
    private final CcdDataService ccdDataService;
    private final CamundaClient camundaClient;
    private final PermissionsService permissionsService;
    private final AuthTokenGenerator camundaServiceAuthTokenGenerator;

    public MapCaseDetailsService(CcdDataService ccdDataService,
                                 CamundaClient camundaClient,
                                 PermissionsService permissionsService,
                                 @Qualifier("camundaServiceAuthTokenGenerator")
                                     AuthTokenGenerator camundaServiceAuthTokenGenerator) {
        this.ccdDataService = ccdDataService;
        this.camundaClient = camundaClient;
        this.permissionsService = permissionsService;
        this.camundaServiceAuthTokenGenerator = camundaServiceAuthTokenGenerator;
    }

    public Map<String, Object> getMappedDetails(String caseId) {
        String caseData = ccdDataService.getCaseData(caseId);

        try {
            CaseDetails caseDetails = new ObjectMapper().readValue(caseData, CaseDetails.class);

            String jurisdiction = caseDetails.getJurisdiction();
            String caseType = caseDetails.getCaseTypeId();

            List<DecisionTableResult> decisionTableResults = camundaClient.mapCaseData(
                camundaServiceAuthTokenGenerator.generate(),
                MAP_CASE_DATA_DECISION_TABLE_NAME,
                jurisdiction,
                caseType,
                new DmnRequest<>(
                    new DecisionTableRequest(jsonValue(caseData))
                )
            );

            List<DecisionTableResult> permissionsDmnResults =
                permissionsService.getMappedDetails(jurisdiction, caseType, caseData);

            Map<String, Object> mappedCaseDetails =
                Stream.concat(decisionTableResults.stream(), permissionsDmnResults.stream())
                    .collect(toMap(
                        mapCaseDataDmnResult -> mapCaseDataDmnResult.getName().getValue(),
                        mapCaseDataDmnResult -> mapCaseDataDmnResult.getValue().getValue()
                    ));

            HashMap<String, Object> allMappedDetails = new HashMap<>(mappedCaseDetails);
            allMappedDetails.put("securityClassification", caseDetails.getSecurityClassification());
            allMappedDetails.put("caseType", caseDetails.getCaseTypeId());
            return allMappedDetails;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse result from CCD for [" + caseId + "]", e);
        }
    }
}
