package com.k9x.infrastructure.out.json.disciplines.obdx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SimplifiedFederationConfigurationDTO(String id, String country, List<Exercise> exercises) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Exercise(String id) {
    }
}