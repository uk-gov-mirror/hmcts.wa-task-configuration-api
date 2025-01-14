package uk.gov.hmcts.reform.wataskconfigurationapi.utils;

import uk.gov.hmcts.reform.wataskconfigurationapi.controllers.PostConfigureTaskTest;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue;
import uk.gov.hmcts.reform.wataskconfigurationapi.services.CreateTaskMessage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.ZonedDateTime.now;
import static uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda.CamundaValue.stringValue;

public class CreateTaskMessageBuilder {
    private String messageName;
    private Map<String, CamundaValue<?>> processVariables;

    public CreateTaskMessageBuilder withMessageName(String messageName) {
        this.messageName = messageName;
        return this;
    }

    public CreateTaskMessageBuilder withProcessVariables(Map<String, CamundaValue<?>> processVariables) {
        this.processVariables = processVariables;
        return this;
    }

    public CreateTaskMessageBuilder withCaseId(String caseId) {
        processVariables.put("caseId", stringValue(caseId));
        processVariables.put("taskId", stringValue("wa-task-configuration-api-task"));
        processVariables.put("group", stringValue("TCW"));
        return this;
    }

    public CreateTaskMessage build() {
        return new CreateTaskMessage(messageName, processVariables);
    }

    public static CreateTaskMessageBuilder createBasicMessageForTask() {
        HashMap<String, CamundaValue<?>> processVariables = new HashMap<>();
        processVariables.put("caseId", stringValue(UUID.randomUUID().toString()));
        processVariables.put("taskId", stringValue("wa-task-configuration-api-task"));
        processVariables.put("group", stringValue("TCW"));
        processVariables.put(
            "dueDate",
            stringValue(now().plusDays(2).format(PostConfigureTaskTest.CAMUNDA_DATA_TIME_FORMATTER))
        );
        processVariables.put("name", stringValue("task name"));
        processVariables.put(
            "delayUntil",
            stringValue(ZonedDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        return new CreateTaskMessageBuilder()
            .withMessageName("createTaskMessage")
            .withProcessVariables(processVariables);
    }
}
