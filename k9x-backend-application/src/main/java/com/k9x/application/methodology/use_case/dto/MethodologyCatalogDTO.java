package com.k9x.application.methodology.use_case.dto;

import com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;

import java.util.List;
import java.util.Map;

/**
 * @param configurations           the current version of every OBDX configuration, grouped by federation in
 *                                 catalogue order.
 * @param categoryNames            the name of every event category.
 * @param qualificationEnglishNames the English abbreviation of each qualification id (e.g. {@code MB -> VG}).
 */
public record MethodologyCatalogDTO(List<Configuration> configurations,
                                    Map<ObdxEventCategory, LocalizedTextDTO> categoryNames,
                                    Map<String, String> qualificationEnglishNames) {

    /** The current configuration of a rank band, which the catalogue must hold. */
    public Configuration configurationOf(ObdxConfigurationsRankThresholds band) {
        return configurations.stream()
                .filter(configuration -> ObdxConfigurationsRankThresholds.fromConfigurationId(
                        configuration.configurationId()) == band)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No current configuration for " + band.configurationKey()));
    }

    /**
     * @param federationId    short federation id, e.g. {@code FCI}.
     * @param federationName  the federation's full name, a proper noun.
     * @param configurationId the versioned configuration id, e.g. {@code OBDX.FCI_GRADE_3.V2022}.
     * @param name            the configuration's name.
     */
    public record Configuration(String federationId, String federationName, String configurationId,
                                LocalizedTextDTO name) {
    }
}
