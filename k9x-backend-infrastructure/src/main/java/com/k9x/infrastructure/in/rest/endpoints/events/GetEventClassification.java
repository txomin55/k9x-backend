package com.k9x.infrastructure.in.rest.endpoints.events;

import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationDTO;
import com.k9x.application.events.use_cases.GetEventClassificationServiceCase;
import com.k9x.oas.stub.api.EventsFetchClassificationApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public class GetEventClassification implements EventsFetchClassificationApiDelegate {

    private final GetEventClassificationServiceCase getClassificationServiceCase;
    private final MessageSource messageSource;

    public GetEventClassification(GetEventClassificationServiceCase getClassificationServiceCase,
            MessageSource messageSource) {
        this.getClassificationServiceCase = getClassificationServiceCase;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<StageEventClassificationResponseDTO> fetchEventClassification(String eventId) {
        FetchClassificationDTO dto = getClassificationServiceCase.getClassification(eventId);

        return ResponseEntity.ok(new StageEventClassificationResponseDTO(
                new IdNameDTO(dto.configurationId(), dto.configurationId()),
                new IdNameDTO(dto.eventName(), dto.eventId()),
                new IdNameDTO(dto.stageName(), dto.stageId()),
                new IdNameDTO(dto.configurationId(), dto.configurationId()),
                dto.obdx() == null || dto.obdx().scoresLastUpdate() == null ? null
                        : BigDecimal.valueOf(dto.obdx().scoresLastUpdate()), // TODO: use Long once StageEventClassificationResponseDTO.lastUpdated is updated in OAS
                dto.obdx() == null ? null
                        : new ObdxStageEventClassificationResponseDTO(mapCompetitors(dto.obdx().competitors()))));
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
                                        new IdNameDTO(resolveExerciseName(e.exerciseId()), e.exerciseId()),
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

    private String resolveExerciseName(String exerciseId) {
        if (exerciseId == null) {
            return null;
        }
        return messageSource.getMessage(exerciseId, null, exerciseId, LocaleContextHolder.getLocale());
    }
}
