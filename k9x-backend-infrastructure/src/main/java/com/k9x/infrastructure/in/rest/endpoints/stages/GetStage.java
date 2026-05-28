package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.application.stages.use_case.GetStageServiceCase;
import com.k9x.oas.stub.api.StagesFetchOneApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.StageDetailResponseDTO;
import com.k9x.oas.stub.model.StageEventDetailResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetStage implements StagesFetchOneApiDelegate {

    private final GetStageServiceCase getStageServiceCase;

    public GetStage(GetStageServiceCase getStageServiceCase) {
        this.getStageServiceCase = getStageServiceCase;
    }

    @Override
    public ResponseEntity<StageDetailResponseDTO> fetchStage(String id) {
        var stage = getStageServiceCase.getStage(id);
        return ResponseEntity.ok(new StageDetailResponseDTO(
                stage.id(),
                stage.name(),
                stage.dateFrom(),
                stage.dateTo(),
                stage.events().stream()
                        .map(e -> new StageEventDetailResponseDTO(
                                e.id(),
                                e.name(),
                                new IdNameDTO(e.configurationId(), e.disciplineName()),
                                List.of()))
                        .toList(),
                List.of(),
                stage.address(),
                stage.organizer()));
    }
}
