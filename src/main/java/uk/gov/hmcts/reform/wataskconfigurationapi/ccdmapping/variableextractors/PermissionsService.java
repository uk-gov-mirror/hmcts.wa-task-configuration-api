package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;

import java.util.List;

import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;

@Component
public class PermissionsService {
    public static final String PERMISSION_DECISION_TABLE_NAME = "permissions";
    private final CamundaClient camundaClient;
    private final AuthTokenGenerator camundaServiceAuthTokenGenerator;

    public PermissionsService(CamundaClient camundaClient,
                              @Qualifier("camundaServiceAuthTokenGenerator")
                                  AuthTokenGenerator camundaServiceAuthTokenGenerator) {
        this.camundaClient = camundaClient;
        this.camundaServiceAuthTokenGenerator = camundaServiceAuthTokenGenerator;
    }

    public List<DecisionTableResult> getMappedDetails(String jurisdiction, String caseType, String caseData) {

        try {
            return camundaClient.mapCaseData(
                camundaServiceAuthTokenGenerator.generate(),
                PERMISSION_DECISION_TABLE_NAME,
                jurisdiction,
                caseType,
                new DmnRequest<>(
                    new DecisionTableRequest(jsonValue(caseData))
                )
            );

        } catch (FeignException e) {
            throw new IllegalStateException(
                "Could not evaluate from decision table [" + PERMISSION_DECISION_TABLE_NAME + "]",
                e
            );
        }
    }
}
