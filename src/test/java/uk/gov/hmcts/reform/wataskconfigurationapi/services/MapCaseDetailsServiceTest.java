package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CamundaServiceApi;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DmnRequest;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.stringValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.services.MapCaseDetailsService.WA_TASK_CONFIGURATION_DECISION_TABLE_NAME;

@ExtendWith(MockitoExtension.class)
class MapCaseDetailsServiceTest {

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";
    @Mock
    private CamundaServiceApi camundaServiceApi;
    @Mock
    private CcdDataService ccdDataService;
    @Mock
    private PermissionsService permissionsService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Spy
    private ObjectMapper objectMapper;

    private MapCaseDetailsService mapCaseDetailsService;

    @BeforeEach
    void setUp() {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapCaseDetailsService = new MapCaseDetailsService(
            ccdDataService,
            camundaServiceApi,
            permissionsService,
            authTokenGenerator,
            objectMapper);
    }

    @Test
    void doesNotHaveAnyFieldsToMap() {
        String someCaseId = "someCaseId";
        String ccdData = "{"
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";
        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        when(ccdDataService.getCaseData(someCaseId)).thenReturn(ccdData);
        when(permissionsService.getMappedDetails("ia", "Asylum", ccdData))
            .thenReturn(asList(
                new DecisionTableResult(
                    stringValue("tribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel")),
                new DecisionTableResult(
                    stringValue("seniorTribunalCaseworker"), stringValue("Read,Refer,Own,Manage,Cancel"))
            ));

        when(camundaServiceApi.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_CONFIGURATION_DECISION_TABLE_NAME,
            "ia",
            "asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(ccdData)))
            )
        ).thenReturn(emptyList());


        HashMap<String, Object> expectedMappedData = new HashMap<>();
        expectedMappedData.put("tribunalCaseworker", "Read,Refer,Own,Manage,Cancel");
        expectedMappedData.put("seniorTribunalCaseworker", "Read,Refer,Own,Manage,Cancel");
        expectedMappedData.put("securityClassification", "PUBLIC");
        expectedMappedData.put("caseType", "Asylum");
        Map<String, Object> mappedData = mapCaseDetailsService.getMappedDetails(someCaseId);

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

                Map<String, Object> mappedData = mapCaseDetailsService.getMappedDetails(someCaseId);

                assertThat(mappedData, is(emptyMap()));
            }
        );
    }

    @Test
    void getsFieldsToMap() {
        String someCaseId = "someCaseId";
        String ccdData = "{ "
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
        when(ccdDataService.getCaseData(someCaseId)).thenReturn(ccdData);
        when(camundaServiceApi.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_CONFIGURATION_DECISION_TABLE_NAME,
            "ia",
            "asylum",
            new DmnRequest<>(new DecisionTableRequest(jsonValue(ccdData)))
        ))
            .thenReturn(asList(
                new DecisionTableResult(stringValue("name1"), stringValue("value1")),
                new DecisionTableResult(stringValue("name2"), stringValue("value2"))
            ));

        Map<String, Object> expectedMappedData = Map.of(
            "name1", "value1",
            "name2", "value2",
            "securityClassification", "PUBLIC",
            "caseType", "Asylum");

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        Map<String, Object> mappedData = mapCaseDetailsService.getMappedDetails(someCaseId);

        assertThat(mappedData, is(expectedMappedData));

    }
}
