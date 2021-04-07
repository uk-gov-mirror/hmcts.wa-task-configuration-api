package uk.gov.hmcts.reform.wataskconfigurationapi.consumer.ccd;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Maps;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.wataskconfigurationapi.SpringBootContractBaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CcdCasePactTest extends SpringBootContractBaseTest {

    private static final String TEST_CASE_ID = "1607103938250138";
    private static final String CCD_CASE_URL = "/cases/" + TEST_CASE_ID;
    private static final String CCD_START_FOR_CASEWORKER = "/caseworkers/0000/jurisdictions/ia/case-types/"
                                                           + "asylum/event-triggers/tester/token";
    private static final String CCD_SUBMIT_EVENT_FOR_CASEWORKER = "/caseworkers/00/jurisdictions/ia/"
                                                                  + "case-types/asylum/cases/0000/events";

    @Pact(provider = "ccd_data_store", consumer = "wa_task_configuration_api")
    public RequestResponsePact ccdGetCasesId(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a GET /cases/{caseId} request from a WA API")
            .path(CCD_CASE_URL)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(createCasesResponse())
            .toPact();
    }

    @Pact(provider = "ccd_data_store", consumer = "wa_task_configuration_api")
    public RequestResponsePact submitEventForCaseworker(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");

        return builder
            .given("Submit event creation as Case worker")
            .uponReceiving("Complete the event creation processrequest from a WA API")
            .path(CCD_SUBMIT_EVENT_FOR_CASEWORKER)
            .method(HttpMethod.POST.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(eventsCasesResponse())
            .toPact();
    }

    @Pact(provider = "ccd_data_store", consumer = "wa_task_configuration_api")
    public RequestResponsePact startForCaseworker(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");

        return builder
            .given("Start event creation as Case worker")
            .uponReceiving("Start the event creation process for an existing case from a WA API")
            .path(CCD_START_FOR_CASEWORKER)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(startForCaseworkerResponse())
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "ccdGetCasesId")
    public void should_post_to_token_endpoint_and_receive_access_token_with_200_response(MockServer mockServer)
        throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .contentType(ContentType.URLENC)
                .log().all(true)
                .get(mockServer.getUrl() + CCD_CASE_URL)
                .then()
                .extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
        assertThat(response.getString("callback_response_status")).isNotBlank();
        assertThat(response.getString("callback_response_status_code")).isEqualTo("0");
        assertThat(response.getString("case_type")).isNotBlank();

    }


    @Test
    @PactTestFor(pactMethod = "startForCaseworker")
    public void should_get_start_for_caseworker_to_token_endpoint(MockServer mockServer)
        throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .contentType(ContentType.URLENC)
                .log().all(true)
                .get(mockServer.getUrl() + CCD_START_FOR_CASEWORKER)
                .then()
                .extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
        assertThat(response).isNotNull();
    }

    @Test
    @PactTestFor(pactMethod = "submitEventForCaseworker")
    public void should_post_to_submit_event_for_caseworker_endpoint_with_200_response(MockServer mockServer)
        throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .contentType(ContentType.URLENC)
                .log().all(true)
                .post(mockServer.getUrl() + CCD_SUBMIT_EVENT_FOR_CASEWORKER)
                .then()
                .extract().asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(response).isNotNull();
        assertThat(response.getString("case_reference")).isEqualTo("string");


    }

    private PactDslJsonBody createCasesResponse() {
        return new PactDslJsonBody()
            .object("after_submit_callback_response")
                .stringType("confirmation_body", "string")
                .stringType("confirmation_header", "string")
            .close()
            .asBody()
            .stringValue("callback_response_status", "string")
            .numberValue("callback_response_status_code", 0)
            .stringValue("case_type", "string")
            .stringValue("created_on", "2021-03-24T09:08:32.869Z")
            .close()
            .object("data")
                .object("additionalProp1", new PactDslJsonBody())
                .object("additionalProp2", new PactDslJsonBody())
                .object("additionalProp3", new PactDslJsonBody())
            .close()
            .object("data_classification")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
                 .stringValue("delete_draft_response_status", "string")
                 .numberValue("delete_draft_response_status_code", 0)
                 .stringValue("id", "string")
                 .stringValue("jurisdiction", "string")
                 .stringValue("last_modified_on", "2021-03-24T09:08:32.869Z")
                 .stringValue("last_state_modified_on", "2021-03-24T09:08:32.869Z")
            .close()
            .object("links")
                .booleanValue("empty", true)
            .close()
            .asBody()
                    .stringValue("security_classification", "PRIVATE")
                    .stringValue("state", "string");

    }

    private PactDslJsonBody eventsCasesResponse() {

        return new PactDslJsonBody()
            .stringType("case_reference", "string")
            .object("data")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .object("data_classification")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
            .stringValue("draft_id", "string")
            .object("event")
            .numberValue("delete_draft_response_status_code", 0)
            .stringValue("description", "string")
            .stringValue("id", "string")
            .stringValue("summary", "string")
            .close()
            .object("event_data")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
            .stringValue("event_token", "PRIVATE")
            .booleanValue("ignore_warning", true)
            .stringValue("on_behalf_of_token", "string")
            .stringValue("security_classification", "string");

    }

    private PactDslJsonBody startForCaseworkerResponse() {

        return new PactDslJsonBody()
            .object("case_details")
            .object("after_submit_callback_response")
            .stringValue("confirmation_body", "string")
            .stringValue("confirmation_header", "string")
            .close()
            .asBody()
            .stringType("callback_response_status", "string")
            .numberValue("callback_response_status_code", 0)
            .object("case_data")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
            .stringValue("case_type_id", "string")
            .stringValue("created_date", "2021-04-07T08:51:52.452Z")
            .object("data_classification")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
            .stringValue("delete_draft_response_status", "string")
            .numberValue("delete_draft_response_status_code", 0)
            .stringValue("id", "string")
            .stringValue("jurisdiction", "string")
            .stringValue("last_modified_on", "2021-03-24T09:08:32.869Z")
            .stringValue("last_state_modified_on", "2021-03-24T09:08:32.869Z")
            .stringValue("security_classification", "PRIVATE")
            .object("security_classifications")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
            .stringValue("state", "string")
           .object("supplementary_data")
            .object("additionalProp1", new PactDslJsonBody())
            .object("additionalProp2", new PactDslJsonBody())
            .object("additionalProp3", new PactDslJsonBody())
            .close()
            .asBody()
            .numberValue("version", 0)
            .close()
            .asBody()
            .stringValue("event_id", "string")
            .stringValue("token", "string");


    }

}

