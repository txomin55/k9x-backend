package com.k9x.application.disciplines.obdx.use_case.dto;

import java.util.List;

public record ObdxConfigurationsDTO(ObdxFederationInfoDTO info, List<ObdxConfigurationDTO> configurations) {
}
