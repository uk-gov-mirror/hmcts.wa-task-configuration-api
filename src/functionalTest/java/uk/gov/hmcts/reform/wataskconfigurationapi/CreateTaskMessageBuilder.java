package uk.gov.hmcts.reform.wataskconfigurationapi;

import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.ZonedDateTime.now;
import static uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.CamundaValue.stringValue;

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

    public CreateTaskMessage build() {
        return new CreateTaskMessage(messageName, processVariables);
    }

    public static CreateTaskMessageBuilder createBasicMessageForTask() {
        HashMap<String, CamundaValue<?>> processVariables = new HashMap<>();
        processVariables.put("ccdId", stringValue(UUID.randomUUID().toString()));
        processVariables.put("taskId", stringValue("wa-task-configuration-api-task"));
        processVariables.put("group", stringValue("TCW"));
        processVariables.put(
            "dueDate",
            stringValue(now().plusDays(2).format(ConfigureTaskTest.CAMUNDA_DATA_TIME_FORMATTER))
        );
        return new CreateTaskMessageBuilder()
            .withMessageName("createTaskMessage")
            .withProcessVariables(processVariables);
    }
}
