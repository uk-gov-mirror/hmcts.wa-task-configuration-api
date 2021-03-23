package uk.gov.hmcts.reform.wataskconfigurationapi;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CcdCasePactTest extends SpringBootContractBaseTest {


    private static final String TEST_CASE_ID = "1607103938250138";
    private static final String CCD_CASE_URL = "/cases/" + TEST_CASE_ID;

    @Pact(provider = "ccd_case", consumer = "wa_task_configuration_api")
    public RequestResponsePact executeGetCaseById(PactDslWithProvider builder) {

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a Get /cases/{case_id} request from a WA API")
            .path(CCD_CASE_URL)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseheaders)
            .body("kjnkbhgvfg")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetCaseById")
    public void should_get_case_id_and_receive_access_token_with_200_response(MockServer mockServer)
        throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, SpringBootContractBaseTest.AUTHORIZATION_BEARER_TOKEN);
        headers.put("ServiceAuthorization", SpringBootContractBaseTest.SERVICE_BEARER_TOKEN);
        headers.put("experimental","true");



        String actualResponseBody =
            SerenityRest
                .given()
                .contentType(ContentType.URLENC)
                .headers(headers)
                .log().all(true)
                .pathParam("caseId", TEST_CASE_ID)
                .get(mockServer.getUrl() + CCD_CASE_URL)
                .then()
                .extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);

        System.out.println(response);
        assertThat(response).isNotNull();

    }
}
