package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.camunda;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CamundaTimeTest {

    @Test
    void camundaTime() {
        String fixedDate = "2020-12-17T11:03:04.462546+01:00[Europe/London]";
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(fixedDate);

        String actualFormattedDate = zonedDateTime.format(CamundaTime.CAMUNDA_DATA_TIME_FORMATTER);

        String expectedFormattedDate = "2020-12-17T10:03:04.462+0000";
        assertThat(actualFormattedDate).isEqualTo(expectedFormattedDate);

        ZonedDateTime actualZonedDateTime = ZonedDateTime.parse(
            actualFormattedDate,
            CamundaTime.CAMUNDA_DATA_TIME_FORMATTER
        );
        String expectedZonedDateTime = "2020-12-17T10:03:04.462Z";
        assertThat(actualZonedDateTime).isEqualTo(expectedZonedDateTime);
    }
}
