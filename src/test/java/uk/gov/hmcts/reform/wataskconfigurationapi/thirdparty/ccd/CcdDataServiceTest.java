package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.idam.IdamSystemTokenGenerator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdDataServiceTest {

    @Mock
    CcdClient ccdClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    IdamSystemTokenGenerator idamSystemTokenGenerator;

    private CcdDataService ccdDataService;

    @Before
    public void setUp() {
        ccdDataService = new CcdDataService(ccdClient, authTokenGenerator, idamSystemTokenGenerator);
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

        when(ccdClient.getCase(userToken, serviceToken, caseId)).thenReturn(caseData);

        String actualCaseData = ccdDataService.getCaseData(caseId);

        assertEquals(caseData, actualCaseData);
    }
}
