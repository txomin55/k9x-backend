package com.k9x.infrastructure.in.rest.endpoints.secured.discipline.obdx;

import com.k9x.application.disciplines.obdx.use_case.GetObdxFederationsConfigurationsServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDisciplinesFetchAllApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetObdxFederationsConfigurations implements SecuredDisciplinesFetchAllApiDelegate {

    private final GetObdxFederationsConfigurationsServiceCase getObdxFederationsConfigurationsServiceCase;
    private final UserInfoDTO userDetails;

    public GetObdxFederationsConfigurations(GetObdxFederationsConfigurationsServiceCase getObdxFederationsConfigurationsServiceCase,
                                            UserInfoDTO userDetails) {
        this.getObdxFederationsConfigurationsServiceCase = getObdxFederationsConfigurationsServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<DisciplineFederationConfigurationResponseDTO>> fetchDisciplinesSecured() {
        List<FederationConfigurationsResponseDTO> federations = getObdxFederationsConfigurationsServiceCase
                .getDisciplineConfigurations(userDetails.isOrganizer()).stream()
                .map(f -> new FederationConfigurationsResponseDTO(
                        new FederationConfigurationResponseDTO(f.info().id(), f.info().name(), f.info().country()),
                        f.configurations().stream()
                                .map(c -> new ConfigurationResponseDTO(c.id(), c.name(),
                                        c.exercises().stream()
                                                .map(e -> new ConfigurationExerciseResponseDTO(e.id(), e.name()))
                                                .toList()))
                                .toList()))
                .toList();

        return ResponseEntity.ok(List.of(
                new DisciplineFederationConfigurationResponseDTO("OBDX", federations)
        ));
    }
}
