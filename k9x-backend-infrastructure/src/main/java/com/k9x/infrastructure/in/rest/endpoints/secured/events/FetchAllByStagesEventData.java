package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.obdx.use_case.GetObdxEventListServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsFetchAllByStagesApiDelegate;
import com.k9x.oas.stub.model.EventDetailResponseDTO;
import com.k9x.oas.stub.model.IdNameDTO;
import com.k9x.oas.stub.model.ObdxEventDetailResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class FetchAllByStagesEventData implements SecuredEventsFetchAllByStagesApiDelegate {

    private final GetObdxEventListServiceCase getObdxEventListServiceCase;
    private final UserInfoDTO userDetails;

    public FetchAllByStagesEventData(GetObdxEventListServiceCase getObdxEventListServiceCase, UserInfoDTO userDetails) {
        this.getObdxEventListServiceCase = getObdxEventListServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<EventDetailResponseDTO>> getStagesEventsSecured(String id, List<String> ids, Object body) {
        return ResponseEntity.ok(
                getObdxEventListServiceCase.getEvents(ids, userDetails.getEmail(), userDetails.isOrganizer())
                        .stream()
                        .map(event -> new EventDetailResponseDTO(
                                new ObdxEventDetailResponseDTO(
                                        event.id(),
                                        new IdNameDTO(event.stageId(), event.stageName()),
                                        event.name(),
                                        null,
                                        null,
                                        List.of(),
                                        List.of(),
                                        null,
                                        List.of()
                                )
                        ))
                        .toList()
        );
    }
}
