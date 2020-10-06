package uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccd.CcdDataService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue.jsonValue;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue.stringValue;

class MapCaseDetailsServiceTest {
    @Test
    void doesNotHaveAnyFieldsToMap() {
        CcdDataService ccdDataService = mock(CcdDataService.class);
        String someCcdId = "someCcdId";
        String ccdData = "ccdData";
        when(ccdDataService.getCaseData(someCcdId)).thenReturn(ccdData);
        CamundaClient camundaClient = mock(CamundaClient.class);
        when(camundaClient.mapCaseData(new DmnRequest<>(new MapCaseDataDmnRequest(stringValue(ccdData)))))
            .thenReturn(emptyList());

        Map<String, Object> mappedData = new MapCaseDetailsService(ccdDataService, camundaClient)
            .getMappedDetails(someCcdId);

        assertThat(mappedData, is(emptyMap()));
    }

    @Test
    void getsFieldsToMap() {
        CcdDataService ccdDataService = mock(CcdDataService.class);
        String someCcdId = "someCcdId";
        String ccdData = "ccdData";
        when(ccdDataService.getCaseData(someCcdId)).thenReturn(ccdData);
        CamundaClient camundaClient = mock(CamundaClient.class);
        when(camundaClient.mapCaseData(new DmnRequest<>(new MapCaseDataDmnRequest(jsonValue(ccdData)))))
            .thenReturn(asList(new MapCaseDataDmnResult(stringValue("name1"), stringValue("value1")),
                               new MapCaseDataDmnResult(stringValue("name2"), stringValue("value2"))));

        Map<String, Object> mappedData = new MapCaseDetailsService(ccdDataService,
                                                                   camundaClient
        ).getMappedDetails(someCcdId);

        HashMap<String, Object> expectedMappedData = new HashMap<>();
        expectedMappedData.put("name1", "value1");
        expectedMappedData.put("name2", "value2");

        assertThat(mappedData, is(expectedMappedData));
    }
}
