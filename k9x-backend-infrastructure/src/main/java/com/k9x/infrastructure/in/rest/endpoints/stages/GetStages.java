package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.oas.stub.api.StagesFetchAllApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class GetStages implements StagesFetchAllApiDelegate {

    @Override
    public ResponseEntity<List<StageSummaryResponseDTO>> fetchAllStages() {
        return ResponseEntity.ok(List.of(
                new StageSummaryResponseDTO(
                        "stage-1",
                        "Stage One",
                        "Mocked stage description",
                        "ES",
                        new CompetitionLocationDetailResponseDTO("Calle Mayor 1, Madrid", new BigDecimal("40.4168"), new BigDecimal("-3.7038")),
                        1747000000L,
                        1747100000L,
                        List.of(
                                new StageEventSummaryResponseDTO("event-1", "Obedience Open", new IdNameDTO("disc-1", "Obedience"), 5, "OPEN")
                        ),
                        "OPEN",
                        "Mocked Organizer"
                )
        ));
    }
}
