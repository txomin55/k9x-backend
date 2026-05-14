package com.k9x.infrastructure.in.rest.endpoints.secured.discipline;

import com.k9x.oas.stub.api.SecuredDisciplinesFetchAllApiDelegate;
import com.k9x.oas.stub.model.DisciplineFederationConfigurationResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetDisciplines implements SecuredDisciplinesFetchAllApiDelegate {

    @Override
    public ResponseEntity<List<DisciplineFederationConfigurationResponseDTO>> fetchDisciplinesSecured() {
        return ResponseEntity.ok(List.of(
                new DisciplineFederationConfigurationResponseDTO("OBDX", List.of())
        ));
    }
}
