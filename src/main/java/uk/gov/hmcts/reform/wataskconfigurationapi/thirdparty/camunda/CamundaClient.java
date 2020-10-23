package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
public interface CamundaClient {
    @PostMapping(
        value = "/decision-definition/key/{decisionTableName}_{jurisdiction}_{caseType}/evaluate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<DecisionTableResult> mapCaseData(
        @PathVariable("decisionTableName") String decisionTableName,
        @PathVariable("jurisdiction") String jurisdiction,
        @PathVariable("caseType") String caseType,
        DmnRequest<DecisionTableRequest> requestParameters
    );

    @PostMapping(value = "/task/{id}/localVariables", produces = MediaType.APPLICATION_JSON_VALUE)
    void addLocalVariablesToTask(@PathVariable("id") String taskId, AddLocalVariableRequest addLocalVariableRequest);

    @GetMapping(value = "/task/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    TaskResponse getTask(@PathVariable("id") String taskId);

    @GetMapping(value = "/process-instance/{id}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, CamundaValue<Object>> getProcessVariables(@PathVariable("id") String processInstanceId);
}

