package com.k9x.application.disciplines.use_case.dto;

import java.util.List;

public record ConfigurationsDTO(FederationInfoDTO info, List<ConfigurationDTO> configurations) {
}
