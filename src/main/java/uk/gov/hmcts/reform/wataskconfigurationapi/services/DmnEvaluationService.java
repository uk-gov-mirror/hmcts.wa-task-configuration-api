package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DmnRequest;

import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.jsonValue;

@Component
public class DmnEvaluationService {
    public static final String WA_TASK_PERMISSIONS_DECISION_TABLE_NAME = "wa-task-permissions";
    public static final String WA_TASK_CONFIGURATION_DECISION_TABLE_NAME = "wa-task-configuration";

    private final CamundaServiceApi camundaServiceApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public DmnEvaluationService(CamundaServiceApi camundaServiceApi,
                                AuthTokenGenerator serviceAuthTokenGenerator) {
        this.camundaServiceApi = camundaServiceApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
    }

    public List<DecisionTableResult> evaluateTaskPermissionsDmn(String jurisdiction,
                                                                String caseType,
                                                                String caseData) {
        return performEvaluateDmnAction(WA_TASK_PERMISSIONS_DECISION_TABLE_NAME, jurisdiction, caseType, caseData);
    }


    public List<DecisionTableResult> evaluateTaskConfigurationDmn(String jurisdiction,
                                                                  String caseType,
                                                                  String caseData) {
        return performEvaluateDmnAction(WA_TASK_CONFIGURATION_DECISION_TABLE_NAME, jurisdiction, caseType, caseData);
    }

    private List<DecisionTableResult> performEvaluateDmnAction(String dmnTableName,
                                                               String jurisdiction,
                                                               String caseType,
                                                               String caseData) {
        try {
            return camundaServiceApi.evaluateDmnTable(
                serviceAuthTokenGenerator.generate(),
                dmnTableName,
                jurisdiction.toLowerCase(Locale.getDefault()),
                caseType.toLowerCase(Locale.getDefault()),
                new DmnRequest<>(
                    new DecisionTableRequest(jsonValue(caseData))
                )
            );
        } catch (FeignException e) {
            throw new IllegalStateException(
                String.format("Could not evaluate from decision table %s", dmnTableName),
                e
            );
        }
    }

}
