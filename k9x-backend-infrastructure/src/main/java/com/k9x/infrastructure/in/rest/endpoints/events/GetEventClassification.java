package com.k9x.infrastructure.in.rest.endpoints.events;

import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationDTO;
import com.k9x.application.events.use_cases.GetEventClassificationServiceCase;
import com.k9x.oas.stub.api.EventsFetchClassificationApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class GetEventClassification implements EventsFetchClassificationApiDelegate {

    private final GetEventClassificationServiceCase getClassificationServiceCase;

    public GetEventClassification(GetEventClassificationServiceCase getClassificationServiceCase) {
        this.getClassificationServiceCase = getClassificationServiceCase;
    }

    @Override
    public ResponseEntity<StageEventClassificationResponseDTO> fetchEventClassification(String eventId) {
        FetchClassificationDTO dto = getClassificationServiceCase.getClassification(eventId);

        return ResponseEntity.ok(new StageEventClassificationResponseDTO(
                new IdNameDTO(dto.configurationId(), dto.configurationId()),
                new IdNameDTO(dto.eventName(), dto.eventId()),
                new IdNameDTO(dto.stageName(), dto.stageId()),
                new IdNameDTO(dto.configurationId(), dto.configurationId()),
                BigDecimal.valueOf(dto.scoresLastUpdate()), // TODO: use Long once StageEventClassificationResponseDTO.lastUpdated is updated in OAS
                new ObdxStageEventClassificationResponseDTO(mapCompetitors(dto.competitors()))));
    }

    private List<StageEventClassificationItemResponseDTO> mapCompetitors(
            List<FetchClassificationCompetitorDTO> competitors) {
        return competitors.stream()
                .map(c -> new StageEventClassificationItemResponseDTO(
                        c.country(),
                        c.owner(),
                        c.team(),
                        c.status(), // TODO: map c.tied() once StageEventClassificationItemResponseDTO exposes a tied field
                        new IdNameDTO(c.dogName(), c.dogId()),
                        c.exercises().stream()
                                .map(e -> new StageEventClassificationExerciseScoresResponseDTO(
                                        new IdNameDTO(e.exerciseId(), e.exerciseId()),
                                        e.rawScore(),
                                        e.scoreRating(),
                                        e.totalScore(),
                                        e.tags(),
                                        e.judgeScores().stream()
                                                .map(j -> new StageEventClassificationScoreResponseDTO(
                                                        new IdNameDTO(j.judgeName(), j.judgeId()),
                                                        j.score(),
                                                        j.scoreRating()))
                                                .toList()))
                                .toList(),
                        c.position(),
                        c.totalScore(),
                        c.scoreRating()))
                .toList();
    }
}
