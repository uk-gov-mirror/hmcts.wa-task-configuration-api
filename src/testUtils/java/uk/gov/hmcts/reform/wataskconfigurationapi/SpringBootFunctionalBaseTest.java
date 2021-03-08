package uk.gov.hmcts.reform.wataskconfigurationapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.IdamTokenGenerator;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.idam.entities.UserInfo;
import uk.gov.hmcts.reform.wataskconfigurationapi.config.RestApiActions;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.AuthorizationHeadersProvider;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.CreateTaskMessage;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.RoleAssignmentHelper;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
@Slf4j
public abstract class SpringBootFunctionalBaseTest {
    public static final DateTimeFormatter CAMUNDA_DATA_TIME_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final String ENDPOINT_COMPLETE_TASK = "task/{task-id}/complete";
    private static final String ENDPOINT_HISTORY_TASK = "history/task";

    @Value("${targets.instance}")
    protected String testUrl;
    @Value("${camunda.url}")
    protected String camundaUrl;

    protected RestApiActions restApiActions;
    protected RestApiActions camundaApiActions;

    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired
    private IdamTokenGenerator systemUserIdamToken;
    @Autowired
    private IdamTokenGenerator waTestLawFirmIdamToken;
    @Autowired
    private CoreCaseDataApi coreCaseDataApi;
    @Autowired
    protected RoleAssignmentHelper roleAssignmentHelper;

    @Before
    public void setUpGivens() {

        restApiActions = new RestApiActions(testUrl, SNAKE_CASE).setUp();
        camundaApiActions = new RestApiActions(camundaUrl, LOWER_CAMEL_CASE).setUp();

    }

    public void cleanUp(String taskId) {
        if (StringUtils.isNotBlank(taskId)) {
            camundaApiActions.post(
                ENDPOINT_COMPLETE_TASK,
                taskId,
                new Headers(authorizationHeadersProvider.getServiceAuthorizationHeader())
            );

            await().ignoreException(AssertionError.class)
                .pollInterval(500, MILLISECONDS)
                .atMost(20, SECONDS)
                .until(
                    () -> {

                        Response result = camundaApiActions.get(
                            ENDPOINT_HISTORY_TASK + "?taskId=" + taskId,
                            authorizationHeadersProvider.getServiceAuthorizationHeader()
                        );

                        result.then().assertThat()
                            .statusCode(HttpStatus.OK.value())
                            .body("[0].deleteReason", is("completed"));
                        return true;
                    });
        }
    }

    public AtomicReference<String> getTaskId(Object taskName, String filter) {
        AtomicReference<String> response = new AtomicReference<>();
        await().ignoreException(AssertionError.class)
            .pollInterval(500, MILLISECONDS)
            .atMost(30, SECONDS)
            .until(
                () -> {
                    Response camundaGetTaskResult = camundaApiActions.get(
                        "/task" + filter,
                        authorizationHeadersProvider.getServiceAuthorizationHeader()
                    );
                    camundaGetTaskResult.then().assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body("size()", is(1))
                        .body("[0].name", is(taskName));

                    response.set(camundaGetTaskResult
                                     .then()
                                     .extract()
                                     .path("[0].id"));
                    return true;
                });
        return response;
    }


    public String createCcdCase() throws IOException {
        String userToken = waTestLawFirmIdamToken.generate();
        UserInfo userInfo = waTestLawFirmIdamToken.getUserInfo(userToken);
        String serviceToken = authorizationHeadersProvider.getServiceAuthorizationHeader().getValue();
        StartEventResponse startCase = coreCaseDataApi.startForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            "startAppeal"
        );
        String caseData = new String(
            (Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                                        .getResourceAsStream("case_data.json"))).readAllBytes()
        );
        var data = new ObjectMapper().readValue(caseData, Map.class);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startCase.getToken())
            .event(Event.builder()
                       .id(startCase.getEventId())
                       .summary("summary")
                       .description("description")
                       .build())
            .data(data)
            .build();

        CaseDetails caseDetails = coreCaseDataApi.submitForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            true,
            caseDataContent
        );

        log.info("Created case [" + caseDetails.getId() + "]");

        StartEventResponse submitCase = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            caseDetails.getId().toString(),
            "submitAppeal"
        );

        CaseDataContent submitCaseDataContent = CaseDataContent.builder()
            .eventToken(submitCase.getToken())
            .event(Event.builder()
                       .id(submitCase.getEventId())
                       .summary("summary")
                       .description("description")
                       .build())
            .data(data)
            .build();
        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            "IA",
            "Asylum",
            caseDetails.getId().toString(),
            true,
            submitCaseDataContent
        );
        log.info("Submitted case [" + caseDetails.getId() + "]");
        //Added wait as there seems to be a delay while retrieving the case.
        waitSeconds(2);
        return caseDetails.getId().toString();
    }

    private void waitSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String createTask(CreateTaskMessage createTaskMessage) {

        Response camundaResult = camundaApiActions.post(
            "/message",
            createTaskMessage,
            authorizationHeadersProvider.getServiceAuthorizationHeader()
        );

        camundaResult.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        Object taskName = createTaskMessage.getProcessVariables().get("name").getValue();

        String filter = "?processVariables=" + "caseId_eq_" + createTaskMessage.getCaseId();

        AtomicReference<String> response = getTaskId(taskName, filter);

        return response.get();

    }
}
