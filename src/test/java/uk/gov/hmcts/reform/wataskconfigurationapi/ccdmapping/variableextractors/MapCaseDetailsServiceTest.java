package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CcdDataService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.MapCaseDetailsService.MAP_CASE_DATA_DECISION_TABLE_NAME;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.stringValue;

@ExtendWith(MockitoExtension.class)
class MapCaseDetailsServiceTest {

    @Mock
    private CamundaClient camundaClient;
    @Mock
    private CcdDataService ccdDataService;
    @Mock
    private PermissionsService permissionsService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    @Test
    void doesNotHaveAnyFieldsToMap() {
        String someCaseId = "someCaseId";
        String ccdData = "{"
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type_id\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";
        when(ccdDataService.getCaseData(someCaseId)).thenReturn(ccdData);
        when(permissionsService.getMappedDetails("ia", "Asylum", ccdData))
            .thenReturn(asList(
                new DecisionTableResult(
                    stringValue("tribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel")),
                new DecisionTableResult(
                    stringValue("seniorTribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel"))
            ));
        when(camundaClient.mapCaseData(
            BEARER_SERVICE_TOKEN,
            MAP_CASE_DATA_DECISION_TABLE_NAME,
            "ia",
            "Asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(ccdData)))
             )
        ).thenReturn(emptyList());

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        HashMap<String, Object> expectedMappedData = new HashMap<>();
        expectedMappedData.put("tribunalCaseworker", "Read,Refer,Own,Manage,Cancel");
        expectedMappedData.put("seniorTribunalCaseworker", "Read,Refer,Own,Manage,Cancel");
        expectedMappedData.put("securityClassification", "PUBLIC");
        expectedMappedData.put("caseType", "Asylum");
        Map<String, Object> mappedData = new MapCaseDetailsService(
            ccdDataService,
            camundaClient,
            permissionsService,
            authTokenGenerator
        )
            .getMappedDetails(someCaseId);

        assertThat(mappedData, is(expectedMappedData));
    }

    @Test
    void cannotParseResponseFromCcd() {
        assertThrows(
            IllegalStateException.class,
            () -> {
                String someCaseId = "someCaseId";
                String ccdData = "not valid json";
                when(ccdDataService.getCaseData(someCaseId)).thenReturn(ccdData);

                Map<String, Object> mappedData = new MapCaseDetailsService(
                    ccdDataService,
                    camundaClient,
                    permissionsService,
                        authTokenGenerator)
                    .getMappedDetails(someCaseId);

                assertThat(mappedData, is(emptyMap()));
            }
        );
    }

    @Test
    void getsFieldsToMap() {
        String someCaseId = "someCaseId";
        String ccdData = "{ "
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type_id\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";
        when(ccdDataService.getCaseData(someCaseId)).thenReturn(ccdData);
        when(camundaClient.mapCaseData(
            BEARER_SERVICE_TOKEN,
            MAP_CASE_DATA_DECISION_TABLE_NAME,
            "ia",
            "Asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(ccdData)))
        ))
            .thenReturn(asList(
                new DecisionTableResult(stringValue("name1"), stringValue("value1")),
                new DecisionTableResult(stringValue("name2"), stringValue("value2"))
            ));

        HashMap<String, Object> expectedMappedData = new HashMap<>();
        expectedMappedData.put("name1", "value1");
        expectedMappedData.put("name2", "value2");
        expectedMappedData.put("securityClassification", "PUBLIC");
        expectedMappedData.put("caseType", "Asylum");

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        Map<String, Object> mappedData = new MapCaseDetailsService(
            ccdDataService,
            camundaClient,
            permissionsService,
                authTokenGenerator).getMappedDetails(someCaseId);

        assertThat(mappedData, is(expectedMappedData));
    }
}
