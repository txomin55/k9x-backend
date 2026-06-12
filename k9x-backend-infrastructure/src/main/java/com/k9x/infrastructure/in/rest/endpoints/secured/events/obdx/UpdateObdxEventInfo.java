package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.UpdateObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsUpdateInfoObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateEventRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class UpdateObdxEventInfo implements SecuredEventsUpdateInfoObdxApiDelegate {

    // TODO: valor simulado hasta que el contrato OAS exponga `ring` en cada juez del body.
    private static final Integer SIMULATED_RING = 1;

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
                        body.getCompetitors() == null ? List.of() : body.getCompetitors().stream()
                                .map(c -> new UpdateObdxEventCommand.CompetitorCommand(c.getDogId(), c.getPosition()))
                                .toList(),
                        body.getExercises() == null ? List.of() : body.getExercises().stream()
                                .map(e -> new UpdateObdxEventCommand.ExerciseCommand(e.getId(), e.getPosition(), e.getTags()))
                                .toList(),
                        body.getJudges() == null ? List.of() : body.getJudges().stream()
                                // TODO: el `ring` (numérico del 1 al 100) llegará en el body del OAS junto al
                                //  id del juez y el collector. Mientras el contrato no lo exponga, se simula
                                //  su recepción con SIMULATED_RING; el resto del stack ya lo persiste en la
                                //  columna `ring` de obdx.event_judges.
                                .map(j -> new UpdateObdxEventCommand.JudgeCommand(
                                        j.getId(), j.getCollectorEmail(), SIMULATED_RING))
                                .toList()
                ),
                userDetails.getEmail(),
                userDetails.isOrganizer()
        );
        return ResponseEntity.ok().build();
    }
}
