package com.k9x.infrastructure.in.rest.endpoints.events;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import com.k9x.oas.stub.api.EventsFetchClassificationApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetEventClassification implements EventsFetchClassificationApiDelegate {

    private final GetEventClassificationServiceCase getClassificationServiceCase;
    private final MessageSource messageSource;
    private final ReferenceNameResolver referenceNames;

    public GetEventClassification(GetEventClassificationServiceCase getClassificationServiceCase,
                                  MessageSource messageSource, ReferenceNameResolver referenceNames) {
        this.getClassificationServiceCase = getClassificationServiceCase;
        this.messageSource = messageSource;
        this.referenceNames = referenceNames;
    }

    @Override
    public ResponseEntity<StageEventClassificationResponseDTO> fetchEventClassification(String eventId) {
        FetchClassificationDTO dto = getClassificationServiceCase.getClassification(eventId);

        return ResponseEntity.ok(new StageEventClassificationResponseDTO(
                referenceNames.discipline(dto.disciplineId()),
                new IdNameDTO(dto.eventName(), dto.eventId()),
                dto.rank(),
                new IdNameDTO(dto.stageName(), dto.stageId()),
                new IdNameDTO(dto.configurationName(), dto.configurationId()),
                dto.eventStatus(),
                dto.competitionName(),
                dto.scoresLastUpdate(),
                dto.obdx() == null ? null
                        : new ObdxStageEventClassificationResponseDTO(mapCompetitors(dto.obdx().competitors()),
                                dto.obdx().scoreCalculation(), mapJudges(dto.obdx().judges())),
                dto.source().name()));
    }

    private List<IdNameDTO> mapJudges(List<FetchObdxEventJudgeDTO> judges) {
        return judges.stream()
                .map(j -> new IdNameDTO(j.judgeName(), j.judgeId()))
                .toList();
    }

    private List<StageEventClassificationItemResponseDTO> mapCompetitors(
            List<FetchClassificationCompetitorDTO> competitors) {
        return competitors.stream()
                .map(c -> new StageEventClassificationItemResponseDTO(
                        referenceNames.country(c.country()),
                        c.owner(),
                        c.handler(),
                        c.team(),
                        c.status(),
                        referenceNames.breed(c.breed()),
                        new IdNameDTO(c.dogName(), c.dogIdentification()),
                        c.exercises().stream()
                                .map(e -> new StageEventClassificationExerciseScoresResponseDTO(
                                        new IdNameDTO(resolveExerciseName(e.exerciseId()), e.exerciseId()),
                                        // OAS exerciseScore carries the achieved weighted score; totalScore carries
                                        // the exercise maximum (max * coef).
                                        e.totalScore(),
                                        e.scoreRating(),
                                        e.exerciseScore(),
                                        e.tags(),
                                        e.judgeScores().stream()
                                                .map(j -> new StageEventClassificationScoreResponseDTO(
                                                        new IdNameDTO(j.judgeName(), j.judgeId()),
                                                        j.score(),
                                                        j.scoreRating(),
                                                        j.applies()))
                                                .toList(),
                                        e.yellowCards().stream()
                                                .map(y -> new StageEventClassificationYellowCardResponseDTO(
                                                        new IdNameDTO(y.judgeName(), y.judgeId()),
                                                        y.timestamp()))
                                                .toList(),
                                        e.redCard() == null ? null
                                                : new StageEventClassificationRedCardResponseDTO(
                                                        new IdNameDTO(e.redCard().judgeName(), e.redCard().judgeId()),
                                                        e.redCard().timestamp())))
                                .toList(),
                        c.position(),
                        c.competitorNumber() != null ? c.competitorNumber().intValue() : null,
                        c.totalScore(),
                        c.scoreRating(),
                        c.tied(),
                        c.startOrder() != null ? c.startOrder().intValue() : null,
                        c.bih(),
                        c.reserve(),
                        c.notCompeting(),
                        c.awards().stream().map(a -> new IdNameDTO(a, a)).toList(),
                        c.qualification()))
                .toList();
    }

    private String resolveExerciseName(String exerciseId) {
        if (exerciseId == null) {
            return null;
        }
        return messageSource.getMessage(exerciseId, null, exerciseId, LocaleContextHolder.getLocale());
    }
}
