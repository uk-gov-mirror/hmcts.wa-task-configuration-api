package uk.gov.hmcts.reform.wataskconfigurationapi.services.externalservice;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


import java.util.Map;

import static java.util.Collections.singletonMap;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals"})
@Component
public class HandleWarningExternalService {

    private final String camundaUrl;

    public HandleWarningExternalService(
        @Value("${camunda.url}") String camundaUrl
    ) {
        this.camundaUrl = camundaUrl;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setupClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(camundaUrl)
            .asyncResponseTimeout(10000)
            .build();

        client.subscribe("hasWarnings")
            .lockDuration(1000)
            .handler(this::hasWarnings)
            .open();
    }

    public void hasWarnings(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String hasWarnings = (String) ((Map<?, ?>) externalTask.getVariable("task")).get("hasWarnings");

        if(hasWarnings.equals("No")) {
            Map<String, Object> processVariables = singletonMap(
                "hasWarning",
                "Yes"
            );

            externalTaskService.complete(externalTask, processVariables);
        }
    }
}


