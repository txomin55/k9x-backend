package com.k9x.application.disciplines.obdx.port;

import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;

import java.io.IOException;
import java.util.List;

public interface GetObdxFederationsConfigurationsPort {
    List<ConfigurationsDTO> getConfigurations() throws IOException;
}
