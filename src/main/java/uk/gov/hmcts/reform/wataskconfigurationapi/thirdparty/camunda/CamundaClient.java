package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
public interface CamundaClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(
        value = "/decision-definition/key/{decisionTableName}_{jurisdiction}_{caseType}/evaluate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    List<DecisionTableResult> mapCaseData(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("decisionTableName") String decisionTableName,
        @PathVariable("jurisdiction") String jurisdiction,
        @PathVariable("caseType") String caseType,
        DmnRequest<DecisionTableRequest> requestParameters
    );

    @PostMapping(value = "/task/{id}/localVariables", produces = MediaType.APPLICATION_JSON_VALUE)
    void addLocalVariablesToTask(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                 @PathVariable("id") String taskId,
                                 AddLocalVariableRequest addLocalVariableRequest);

    @GetMapping(value = "/task/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    TaskResponse getTask(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                         @PathVariable("id") String taskId);

    @GetMapping(value = "/process-instance/{id}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, CamundaValue<Object>> getProcessVariables(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("id") String processInstanceId);
}

