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
public class PermissionsService {
    public static final String WA_TASK_PERMISSIONS_DECISION_TABLE_NAME = "wa-task-permissions";
    private final CamundaServiceApi camundaServiceApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public PermissionsService(CamundaServiceApi camundaServiceApi,
                              AuthTokenGenerator serviceAuthTokenGenerator) {
        this.camundaServiceApi = camundaServiceApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
    }

    public List<DecisionTableResult> getMappedDetails(String jurisdiction, String caseType, String caseData) {

        try {
            return camundaServiceApi.evaluateDmnTable(
                serviceAuthTokenGenerator.generate(),
                WA_TASK_PERMISSIONS_DECISION_TABLE_NAME,
                jurisdiction.toLowerCase(Locale.getDefault()),
                caseType.toLowerCase(Locale.getDefault()),
                new DmnRequest<>(
                    new DecisionTableRequest(jsonValue(caseData))
                )
            );

        } catch (FeignException e) {
            throw new IllegalStateException(
                "Could not evaluate from decision table [" + WA_TASK_PERMISSIONS_DECISION_TABLE_NAME + "]",
                e
            );
        }
    }
}
