package com.k9x.infrastructure.out.json.disciplines.obdx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseAllowedValuesConfigurationDTO(@JsonProperty("allowed_values") List<BigDecimal> allowedValues,
                                                    List<Exercise> exercises) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Exercise(String id) {
    }
}
