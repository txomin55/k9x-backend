package com.k9x.infrastructure.in.rest.endpoints.events;

import com.k9x.oas.stub.api.EventsFetchClassificationApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.StageEventClassificationResponseDTO;
import org.springframework.http.ResponseEntity;

public class GetEventClassification implements EventsFetchClassificationApiDelegate {

    @Override
    public ResponseEntity<StageEventClassificationResponseDTO> fetchEventClassification(String stageId, String eventId, Object type) {
        return ResponseEntity.ok(new StageEventClassificationResponseDTO(
                new IdNameDTO("OBDX", "discipline-1"),
                new IdNameDTO("Event One", eventId),
                new IdNameDTO("Stage One", stageId),
                new IdNameDTO("Config One", "config-1"),
                1000000
        ));
    }
}
