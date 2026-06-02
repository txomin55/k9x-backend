package com.k9x.infrastructure.in.rest.endpoints.secured.disciplines;

import com.k9x.application.disciplines.use_case.GetDisciplineFederationsConfigurationsServiceCase;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.DisciplineConfigurationsDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredDisciplinesFetchAllByDisciplineApiDelegate;
import com.k9x.oas.stub.model.ConfigurationResponseDTO;
import com.k9x.oas.stub.model.DisciplineFederationConfigurationResponseDTO;
import com.k9x.oas.stub.model.FederationConfigurationResponseDTO;
import com.k9x.oas.stub.model.FederationConfigurationsResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.ObdxFederationConfigurationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetFederationsConfigurations implements SecuredDisciplinesFetchAllByDisciplineApiDelegate {

    private final GetDisciplineFederationsConfigurationsServiceCase getDisciplineFederationsConfigurationsServiceCase;
    private final UserInfoDTO userDetails;

    public GetFederationsConfigurations(GetDisciplineFederationsConfigurationsServiceCase getDisciplineFederationsConfigurationsServiceCase,
                                        UserInfoDTO userDetails) {
        this.getDisciplineFederationsConfigurationsServiceCase = getDisciplineFederationsConfigurationsServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<DisciplineFederationConfigurationResponseDTO> fetchDisciplinesSecured(String discipline) {
        DisciplineConfigurationsDTO configurations = getDisciplineFederationsConfigurationsServiceCase
                .getDisciplineConfigurations(discipline, userDetails.isOrganizer());

        DisciplineFederationConfigurationResponseDTO response = new DisciplineFederationConfigurationResponseDTO();
        if (configurations.obdx() != null) {
            response.setObdx(new ObdxFederationConfigurationResponseDTO(mapFederations(configurations.obdx())));
        }

        return ResponseEntity.ok(response);
    }

    private List<FederationConfigurationsResponseDTO> mapFederations(List<ConfigurationsDTO> configurations) {
        return configurations.stream()
                .map(f -> new FederationConfigurationsResponseDTO(
                        new FederationConfigurationResponseDTO(f.info().id(), f.info().name(), f.info().country()),
                        f.configurations().stream()
                                .map(c -> new ConfigurationResponseDTO(c.id(), c.name(),
                                        c.exercises().stream()
                                                .map(e -> new IdNameDTO(e.name(), e.id()))
                                                .toList()))
                                .toList()))
                .toList();
    }
}
