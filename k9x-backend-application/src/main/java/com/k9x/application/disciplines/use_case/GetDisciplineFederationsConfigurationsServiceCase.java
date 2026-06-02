package com.k9x.application.disciplines.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.DisciplineConfigurationsDTO;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetDisciplineFederationsConfigurationsServiceCase {

    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    public GetDisciplineFederationsConfigurationsServiceCase(GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
    }

    private static void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    public DisciplineConfigurationsDTO getDisciplineConfigurations(String discipline, boolean organizer) {
        assertOrganizer(organizer);
        Discipline parsedDiscipline = Discipline.valueOf(discipline.toUpperCase(Locale.ROOT));
        return new DisciplineConfigurationsDTO(
                parsedDiscipline == Discipline.OBDX ? getObdxConfigurations() : null);
    }

    private List<ConfigurationsDTO> getObdxConfigurations() {
        try {
            return getObdxFederationsConfigurationsPort.getConfigurations();
        } catch (IOException e) {
            throw new DisciplineConfigurationMalformedException();
        }
    }
}
