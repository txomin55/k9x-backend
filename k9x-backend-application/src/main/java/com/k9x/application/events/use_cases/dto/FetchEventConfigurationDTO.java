package com.k9x.application.events.use_cases.dto;

import com.k9x.application.disciplines.use_case.dto.FederationInfoDTO;

public record FetchEventConfigurationDTO(String id, String name, FederationInfoDTO federation) {
}
