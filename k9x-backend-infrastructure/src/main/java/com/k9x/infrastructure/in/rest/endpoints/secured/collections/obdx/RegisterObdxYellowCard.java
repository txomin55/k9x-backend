package com.k9x.infrastructure.in.rest.endpoints.secured.collections.obdx;

import com.k9x.application.collections.obdx.use_case.RegisterObdxYellowCardServiceCase;
import com.k9x.application.collections.obdx.use_case.command.RegisterObdxYellowCardCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsUpdateYellowCardObdxApiDelegate;
import com.k9x.oas.stub.model.RegisterYellowCardRequestDTO;
import org.springframework.http.ResponseEntity;

public class RegisterObdxYellowCard implements SecuredEventsUpdateYellowCardObdxApiDelegate {

    private final RegisterObdxYellowCardServiceCase registerObdxYellowCardServiceCase;
    private final UserInfoDTO userDetails;

    public RegisterObdxYellowCard(RegisterObdxYellowCardServiceCase registerObdxYellowCardServiceCase, UserInfoDTO userDetails) {
        this.registerObdxYellowCardServiceCase = registerObdxYellowCardServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> registerYellowCard(String eventId, RegisterYellowCardRequestDTO body) {
        registerObdxYellowCardServiceCase.registerYellowCard(
                eventId,
                new RegisterObdxYellowCardCommand(body.getJudgeId(), body.getExerciseId(), body.getDogIdentification()),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
