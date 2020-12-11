package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;

import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;

@Component
public class PermissionsService {
    public static final String WA_TASK_PERMISSIONS_DECISION_TABLE_NAME = "wa-task-permissions";
    private final CamundaClient camundaClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public PermissionsService(CamundaClient camundaClient,
                              AuthTokenGenerator serviceAuthTokenGenerator) {
        this.camundaClient = camundaClient;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
    }

    public List<DecisionTableResult> getMappedDetails(String jurisdiction, String caseType, String caseData) {

        try {
            return camundaClient.evaluateDmnTable(
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
