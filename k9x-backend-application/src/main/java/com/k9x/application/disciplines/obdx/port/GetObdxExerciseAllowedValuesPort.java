package com.k9x.application.disciplines.obdx.port;

import java.math.BigDecimal;
import java.util.List;

public interface GetObdxExerciseAllowedValuesPort {

    List<BigDecimal> getAllowedValues(String exerciseId);
}
