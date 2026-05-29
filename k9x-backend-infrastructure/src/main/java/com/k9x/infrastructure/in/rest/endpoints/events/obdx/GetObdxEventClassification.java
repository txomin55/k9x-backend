package com.k9x.infrastructure.in.rest.endpoints.events.obdx;

import com.k9x.application.events.obdx.use_case.GetObdxEventClassificationServiceCase;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.oas.stub.api.EventsFetchClassificationApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetObdxEventClassification implements EventsFetchClassificationApiDelegate {

    private final GetObdxEventClassificationServiceCase getClassificationServiceCase;

    public GetObdxEventClassification(GetObdxEventClassificationServiceCase getClassificationServiceCase) {
        this.getClassificationServiceCase = getClassificationServiceCase;
    }

    @Override
    public ResponseEntity<StageEventClassificationResponseDTO> fetchEventClassification(String stageId, String eventId, Object type) {
        FetchClassificationDTO dto = getClassificationServiceCase.getClassification(eventId);

        return ResponseEntity.ok(new StageEventClassificationResponseDTO(
                new IdNameDTO(dto.configurationId(), dto.configurationId()),
                new IdNameDTO(dto.eventName(), dto.eventId()),
                new IdNameDTO(dto.stageName(), dto.stageId()),
                new IdNameDTO(dto.configurationId(), dto.configurationId()),
                dto.scoresLastUpdate().intValue(), // TODO: use Long once StageEventClassificationResponseDTO.lastUpdated is updated in OAS
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
