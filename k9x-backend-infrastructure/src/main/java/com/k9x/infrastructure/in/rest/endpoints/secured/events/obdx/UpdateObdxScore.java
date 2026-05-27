package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.UpdateObdxScoreServiceCase;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxScoreCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsUpdateScoreObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateCollectionScoreRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateObdxScore implements SecuredEventsUpdateScoreObdxApiDelegate {

    private final UpdateObdxScoreServiceCase updateObdxScoreServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateObdxScore(UpdateObdxScoreServiceCase updateObdxScoreServiceCase, UserInfoDTO userDetails) {
        this.updateObdxScoreServiceCase = updateObdxScoreServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateObdxScore(String eventId, UpdateCollectionScoreRequestDTO body) {
        updateObdxScoreServiceCase.updateScore(
                eventId,
                new UpdateObdxScoreCommand(body.getJudgeId(), body.getExerciseId(), body.getDogId(), body.getScore()),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
