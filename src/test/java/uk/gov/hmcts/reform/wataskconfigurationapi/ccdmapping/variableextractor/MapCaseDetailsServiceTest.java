package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.MapCaseDetailsService;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaClient;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.DmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.MapCaseDataDmnRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.MapCaseDataDmnResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd.CcdDataService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda.CamundaValue.stringValue;

class MapCaseDetailsServiceTest {

    private CamundaClient camundaClient;
    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {
        camundaClient = mock(CamundaClient.class);
        ccdDataService = mock(CcdDataService.class);
    }

    @Test
    void doesNotHaveAnyFieldsToMap() {
        String someCcdId = "someCcdId";
        String ccdData = "{"
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type_id\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";
        when(ccdDataService.getCaseData(someCcdId)).thenReturn(ccdData);
        when(camundaClient.mapCaseData(
            "ia", "Asylum", new DmnRequest<>(new MapCaseDataDmnRequest(stringValue(ccdData))))
        ).thenReturn(emptyList());

        HashMap<String, Object> expectedMappedData = new HashMap<>();
        expectedMappedData.put("securityClassification", "PUBLIC");
        expectedMappedData.put("caseType", "Asylum");
        Map<String, Object> mappedData = new MapCaseDetailsService(ccdDataService, camundaClient)
            .getMappedDetails(someCcdId);

        assertThat(mappedData, is(expectedMappedData));
    }

    @Test
    void cannotParseResponseFromCcd() {
        assertThrows(
            IllegalStateException.class,
            () -> {
                String someCcdId = "someCcdId";
                String ccdData = "not valid json";
                when(ccdDataService.getCaseData(someCcdId)).thenReturn(ccdData);

                Map<String, Object> mappedData = new MapCaseDetailsService(ccdDataService, camundaClient)
                    .getMappedDetails(someCcdId);

                assertThat(mappedData, is(emptyMap()));
            }
        );
    }

    @Test
    void getsFieldsToMap() {
        String someCcdId = "someCcdId";
        String ccdData = "{ "
                         + "\"jurisdiction\": \"ia\","
                         + "\"case_type_id\": \"Asylum\","
                         + "\"security_classification\": \"PUBLIC\","
                         + "\"data\": {}"
                         + "}";
        when(ccdDataService.getCaseData(someCcdId)).thenReturn(ccdData);
        when(camundaClient.mapCaseData("ia", "Asylum", new DmnRequest<>(new MapCaseDataDmnRequest(jsonValue(ccdData)))))
            .thenReturn(asList(new MapCaseDataDmnResult(stringValue("name1"), stringValue("value1")),
                               new MapCaseDataDmnResult(stringValue("name2"), stringValue("value2"))));

        HashMap<String, Object> expectedMappedData = new HashMap<>();
        expectedMappedData.put("name1", "value1");
        expectedMappedData.put("name2", "value2");
        expectedMappedData.put("securityClassification", "PUBLIC");
        expectedMappedData.put("caseType", "Asylum");

        Map<String, Object> mappedData = new MapCaseDetailsService(ccdDataService,
                                                                   camundaClient
        ).getMappedDetails(someCcdId);

        assertThat(mappedData, is(expectedMappedData));
    }
}
