package uk.gov.hmcts.reform.wataskconfigurationapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AddLocalVariableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.AssigneeRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaTask;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableRequest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DecisionTableResult;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.DmnRequest;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.wataskconfigurationapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
public interface CamundaServiceApi {

    @PostMapping(
        value = "/decision-definition/key/{decisionTableName}-{jurisdiction}-{caseType}/tenant-id/ia/evaluate",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    List<DecisionTableResult> evaluateDmnTable(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("decisionTableName") String decisionTableName,
        @PathVariable("jurisdiction") String jurisdiction,
        @PathVariable("caseType") String caseType,
        DmnRequest<DecisionTableRequest> requestParameters
    );

    @PostMapping(value = "/task/{id}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    void addLocalVariablesToTask(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                 @PathVariable("id") String taskId,
                                 AddLocalVariableRequest addLocalVariableRequest);

    @PostMapping(value = "/task/{id}/assignee", produces = MediaType.APPLICATION_JSON_VALUE)
    void assignTask(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                    @PathVariable("id") String taskId,
                    @RequestBody AssigneeRequest assigneeRequest);

    @GetMapping(value = "/task/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    CamundaTask getTask(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                        @PathVariable("id") String taskId);

    @GetMapping(value = "/task/{id}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, CamundaValue<Object>> getVariables(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("id") String processInstanceId);
}

