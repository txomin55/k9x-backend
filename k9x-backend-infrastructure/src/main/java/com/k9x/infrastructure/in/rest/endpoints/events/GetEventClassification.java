package com.k9x.infrastructure.in.rest.endpoints.events;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.infrastructure.in.rest.mapper.AwardResponseMapper;
import com.k9x.oas.stub.api.EventsFetchClassificationApiDelegate;
import com.k9x.oas.stub.model.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

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
                resolveDiscipline(dto.disciplineId()),
                new IdNameDTO(dto.eventName(), dto.eventId()),
                new IdNameDTO(dto.stageName(), dto.stageId()),
                new IdNameDTO(dto.configurationName(), dto.configurationId()),
                dto.eventStatus(),
                dto.competitionName(),
                dto.scoresLastUpdate(),
                dto.obdx() == null ? null
                        : new ObdxStageEventClassificationResponseDTO(mapCompetitors(dto.obdx().competitors()),
                                dto.obdx().scoreCalculation(), mapJudges(dto.obdx().judges()))));
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
                        resolveCountry(c.country()),
                        c.owner(),
                        c.handler(),
                        c.team(),
                        c.status(),
                        c.breed(),
                        new IdNameDTO(c.dogName(), c.dogId()),
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
                        c.totalScore(),
                        c.scoreRating(),
                        c.tied(),
                        c.startOrder() != null ? c.startOrder().intValue() : null,
                        c.bih(),
                        c.notCompeting(),
                        AwardResponseMapper.toIdNameList(c.awards())))
                .toList();
    }

    private IdNameDTO resolveCountry(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "country." + countryCode.toLowerCase() + ".name", null, countryCode, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, countryCode);
    }

    private String resolveExerciseName(String exerciseId) {
        if (exerciseId == null) {
            return null;
        }
        return messageSource.getMessage(exerciseId, null, exerciseId, LocaleContextHolder.getLocale());
    }

    private IdNameDTO resolveDiscipline(String disciplineId) {
        if (disciplineId == null) {
            return null;
        }
        String name = messageSource.getMessage(
                "discipline." + disciplineId + ".name", null, LocaleContextHolder.getLocale());
        return new IdNameDTO(name, disciplineId);
    }
}
