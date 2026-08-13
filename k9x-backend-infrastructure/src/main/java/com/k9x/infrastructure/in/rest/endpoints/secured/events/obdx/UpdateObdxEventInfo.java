package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import com.k9x.oas.stub.api.SecuredEventsUpdateInfoObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateEventRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class UpdateObdxEventInfo implements SecuredEventsUpdateInfoObdxApiDelegate {

    private final UpdateObdxEventServiceCase updateObdxEventServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateObdxEventInfo(UpdateObdxEventServiceCase updateObdxEventServiceCase, UserInfoDTO userDetails) {
        this.updateObdxEventServiceCase = updateObdxEventServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateObdxEventSecured(String id, UpdateEventRequestDTO body) {
        updateObdxEventServiceCase.updateEvent(
                id,
                new UpdateObdxEventCommand(
                        body.getName(),
                        body.getConfigurationId(),
                        body.getEnrollmentDeadline(),
                        ObdxAvgMethod.valueOf(body.getScoreCalculation()),
                        body.getCompetitors() == null ? List.of() : body.getCompetitors().stream()
                                .map(c -> new UpdateObdxEventCommand.CompetitorCommand(c.getDogIdentification(), c.getPosition(),
                                        c.getCompetitorNumber(),
                                        Boolean.TRUE.equals(c.getBih()), c.getPrimer(),
                                        Boolean.TRUE.equals(c.getReserve())))
                                .toList(),
                        body.getExercises() == null ? List.of() : body.getExercises().stream()
                                .map(e -> new UpdateObdxEventCommand.ExerciseCommand(e.getId(), e.getPosition(),
                                        e.getTags(), e.getJudgesIds()))
                                .toList(),
                        body.getJudges() == null ? List.of() : body.getJudges().stream()
                                .map(j -> new UpdateObdxEventCommand.JudgeCommand(j.getId(), j.getCollectorEmail(),
                                        Boolean.TRUE.equals(j.getMainJudge())))
                                .toList(),
                        body.getAwards() == null ? List.of() : body.getAwards(),
                        body.getCommissioner(),
                        ObdxEventCategory.fromName(body.getCategory())
                ),
                userDetails.getEmail(),
                userDetails.isOrganizer()
        );
        return ResponseEntity.ok().build();
    }
}
