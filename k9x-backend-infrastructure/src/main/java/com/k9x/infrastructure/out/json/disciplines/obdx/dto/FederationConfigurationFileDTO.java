package com.k9x.infrastructure.out.json.disciplines.obdx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FederationConfigurationFileDTO(String id,
                                             String country,
                                             @JsonProperty("allowed_values") List<BigDecimal> allowedValues,
                                             @JsonProperty("break_tie") List<String> breakTie,
                                             @JsonProperty("break_tie_tie") List<String> breakTieTie,
                                             List<Exercise> exercises,
                                             List<Qualification> qualifications) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Exercise(String id, @JsonProperty("coef") BigDecimal coef) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Qualification(String id, @JsonProperty("min_score") BigDecimal minScore) {
    }
}
