package uk.gov.hmcts.reform.wataskconfigurationapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.CamundaProcessVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.ConstantVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.MapCaseDetailsService;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.SystemConfiguredVariableExtractor;
import uk.gov.hmcts.reform.wataskconfigurationapi.ccdmapping.variableextractors.TaskVariableExtractor;

import java.util.List;

import static java.util.Arrays.asList;

@Configuration
public class TaskVariableExtractorConfiguration {
    // Setup this way as the order of the extractors is important as later extractors can override values from
    // earlier ones.
    @Bean
    public List<TaskVariableExtractor> setupTaskVariableExtractors(MapCaseDetailsService mapCaseDetailsService) {
        return asList(
            new ConstantVariableExtractor(),
            new CamundaProcessVariableExtractor(),
            new SystemConfiguredVariableExtractor(mapCaseDetailsService)
        );
    }
}
