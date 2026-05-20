package com.k9x.application.disciplines.obdx.port;

import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationsDTO;

import java.io.IOException;
import java.util.List;

public interface GetObdxFederationsConfigurationsPort {
    List<ObdxConfigurationsDTO> getConfigurations() throws IOException;
}
