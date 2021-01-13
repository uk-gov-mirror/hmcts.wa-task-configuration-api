package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.ccd.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.enums.CamundaVariableDefinition.SECURITY_CLASSIFICATION;

@Component
public class CaseConfigurationProviderService {

    private final CcdDataService ccdDataService;
    private final DmnEvaluationService dmnEvaluationService;
    private final ObjectMapper objectMapper;


    @Autowired
    public CaseConfigurationProviderService(CcdDataService ccdDataService,
                                            DmnEvaluationService dmnEvaluationService,
                                            ObjectMapper objectMapper) {
        this.ccdDataService = ccdDataService;
        this.dmnEvaluationService = dmnEvaluationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Obtains a list of process variables that are related to the ccd case data.
     *
     * @param caseId the ccd case id
     * @return a map with the process variables configuration
     */
    public Map<String, Object> getCaseRelatedConfiguration(String caseId) {
        // Obtain case from ccd
        String caseData = ccdDataService.getCaseData(caseId);
        CaseDetails caseDetails = read(caseData, caseId);

        String jurisdiction = caseDetails.getJurisdiction();
        String caseType = caseDetails.getCaseType();

        // Evaluate Dmns
        List<DecisionTableResult> taskConfigurationDmnResults =
            dmnEvaluationService.evaluateTaskConfigurationDmn(jurisdiction, caseType, caseData);

        List<DecisionTableResult> permissionsDmnResults =
            dmnEvaluationService.evaluateTaskPermissionsDmn(jurisdiction, caseType, caseData);

        // Combine and Collect all dmns results into process variables map
        Map<String, Object> caseConfigurationVariables =
            Stream.concat(taskConfigurationDmnResults.stream(), permissionsDmnResults.stream())
                .collect(toMap(
                    dmnResult -> dmnResult.getName().getValue(),
                    dmnResult -> dmnResult.getValue().getValue()
                ));

        // Enrich case configuration variables with extra variables
        HashMap<String, Object> allCaseConfigurationValues = new HashMap<>(caseConfigurationVariables);
        allCaseConfigurationValues.put(SECURITY_CLASSIFICATION.value(), caseDetails.getSecurityClassification());
        allCaseConfigurationValues.put(CASE_TYPE_ID.value(), caseDetails.getCaseType());
        return allCaseConfigurationValues;

    }

    private CaseDetails read(String caseData, String caseId) {
        try {
            return objectMapper.readValue(caseData, CaseDetails.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                String.format("Cannot parse result from CCD for %s", caseId),
                ex
            );
        }
    }
}
