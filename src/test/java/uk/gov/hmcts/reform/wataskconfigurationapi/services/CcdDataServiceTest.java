package uk.gov.hmcts.reform.wataskconfigurationapi.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.IdamSystemTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.clients.CcdDataServiceApi;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdDataServiceTest {

    @Mock
    CcdDataServiceApi ccdDataServiceApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    IdamSystemTokenGenerator idamSystemTokenGenerator;

    private CcdDataService ccdDataService;

    @Before
    public void setUp() {
        ccdDataService = new CcdDataService(ccdDataServiceApi, authTokenGenerator, idamSystemTokenGenerator);
    }

    @Test
    public void should_get_case_data() {
        String caseId = UUID.randomUUID().toString();
        String userToken = "user_token";
        String serviceToken = "service_token";

        String caseData = "{ "
                          + "\"jurisdiction\": \"ia\", "
                          + "\"case_type_id\": \"Asylum\", "
                          + "\"security_classification\": \"PUBLIC\","
                          + "\"data\": {}"
                          + " }";

        when(idamSystemTokenGenerator.generate()).thenReturn(userToken);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);

        when(ccdDataServiceApi.getCase(userToken, serviceToken, caseId)).thenReturn(caseData);

        String actualCaseData = ccdDataService.getCaseData(caseId);

        assertEquals(caseData, actualCaseData);
    }
}
