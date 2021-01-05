package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DmnRequest;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.stringValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.DmnEvaluationService.WA_TASK_PERMISSIONS_DECISION_TABLE_NAME;

@ExtendWith(MockitoExtension.class)
class DmnEvaluationServiceTest {

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";
    DmnEvaluationService dmnEvaluationService;
    @Mock
    private CamundaServiceApi camundaServiceApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        dmnEvaluationService = new DmnEvaluationService(camundaServiceApi, authTokenGenerator);
    }

    @Test
    void should_succeed_and_return_a_list_of_permissions() {
        String ccdData = "{"
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type_id\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";

        when(camundaServiceApi.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_PERMISSIONS_DECISION_TABLE_NAME,
            "ia",
            "asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(ccdData)))
        ))
            .thenReturn(asList(
                new DecisionTableResult(
                    stringValue("tribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel")),
                new DecisionTableResult(
                    stringValue("seniorTribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel"))
            ));

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        List<DecisionTableResult> response = dmnEvaluationService.evaluateTaskPermissionsDmn("ia", "Asylum", ccdData);

        assertThat(response.size(), is(2));
        assertThat(response, is(asList(
            new DecisionTableResult(
                stringValue("tribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel")),
            new DecisionTableResult(
                stringValue("seniorTribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel"))
        )));
    }


    @Test
    void should_throw_illegal_state_exception_when_feign_exception_is_caught() {
        String ccdData = "{"
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type_id\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";

        when(camundaServiceApi.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_PERMISSIONS_DECISION_TABLE_NAME,
            "ia",
            "asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(ccdData)))
        )).thenThrow(FeignException.class);

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        assertThatThrownBy(() -> dmnEvaluationService.evaluateTaskPermissionsDmn("ia", "Asylum", ccdData))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Could not evaluate from decision table wa-task-permissions")
            .hasCauseInstanceOf(FeignException.class);
    }
}
