package com.k9x.infrastructure.in.rest.endpoints.secured.event;

import com.k9x.oas.stub.api.SecuredEventsFetchAllByStagesApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FetchAllByStagesEventData implements SecuredEventsFetchAllByStagesApiDelegate {

    @Override
    public ResponseEntity<List<EventDetailResponseDTO>> getStagesEventsSecured(String id, List<String> ids, Object body) {
        return ResponseEntity.ok(List.of(
                new EventDetailResponseDTO(
                        new ObdxEventDetailResponseDTO(
                                "event-1",
                                new IdNameDTO("stage-1", "Qualifying Round"),
                                "Mocked Event",
                                "OPEN",
                                new IdNameDTO("disc-1", "Obedience"),
                                List.of(
                                        new EventCompetitorResponseDTO(
                                                "owner-1", "identity-1", "team-1", "ES", 1, "ACTIVE",
                                                new IdNameDTO("dog-1", "Rex")
                                        )
                                ),
                                List.of(
                                        new EventExerciseDetailResponseDTO("ex-1", "Heel on Leash", 1, List.of("basic", "leash"))
                                ),
                                new EventConfigurationDetailResponseDTO(
                                        "cfg-1",
                                        "Standard Config",
                                        new FederationConfigurationResponseDTO("fed-1", "RSCE", "ES")
                                ),
                                List.of(
                                        new EventJudgeDetailResponseDTO("judge-1", "John Doe", "john.doe@example.com")
                                )
                        )
                )
        ));
    }
}
