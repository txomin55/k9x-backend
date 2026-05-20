package com.k9x.application.disciplines.obdx.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationsDTO;
import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.io.IOException;
import java.util.List;

public class GetObdxFederationsConfigurationsServiceCase {

    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    public GetObdxFederationsConfigurationsServiceCase(GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
    }

    private static void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    public List<ObdxConfigurationsDTO> getDisciplineConfigurations(boolean organizer) {
        assertOrganizer(organizer);
        try {
            return getObdxFederationsConfigurationsPort.getConfigurations();
        } catch (IOException e) {
            throw new DisciplineConfigurationMalformedException();
        }
    }
}
