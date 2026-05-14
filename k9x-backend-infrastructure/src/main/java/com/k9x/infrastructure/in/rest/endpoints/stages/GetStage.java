package com.k9x.infrastructure.in.rest.endpoints.stages;

import com.k9x.oas.stub.api.StagesFetchOneApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetStage implements StagesFetchOneApiDelegate {

    @Override
    public ResponseEntity<StageDetailResponseDTO> fetchStage(String id) {
        return ResponseEntity.ok(new StageDetailResponseDTO(
                id,
                "Stage One",
                1747000000L,
                1747100000L,
                List.of(
                        new StageEventDetailResponseDTO(
                                "event-1",
                                "Obedience Open",
                                new IdNameDTO("disc-1", "Obedience"),
                                List.of()
                        )
                ),
                List.of(
                        new CompetitionNotificationDetailResponseDTO("notif-1", 1747000000L, "Stage is now open")
                ),
                "Calle Mayor 1, Madrid",
                "Mocked Organizer"
        ));
    }
}
