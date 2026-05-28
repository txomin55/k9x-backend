package com.k9x.application.disciplines.obdx.port;

import java.math.BigDecimal;
import java.util.List;

public interface GetObdxConfigurationAllowedValuesPort {
    List<BigDecimal> getAllowedValues(String configurationId);
}
