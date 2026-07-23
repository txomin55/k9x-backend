package com.k9x.infrastructure.in.rest.endpoints.secured.users;

import com.k9x.application.users.use_case.RegisterPushSubscriptionServiceCase;
import com.k9x.application.users.use_case.command.RegisterPushSubscriptionCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredUserRegisterPushApiDelegate;
import com.k9x.oas.stub.model.PushSubscriptionRequestDTO;
import org.springframework.http.ResponseEntity;

public class RegisterPush implements SecuredUserRegisterPushApiDelegate {

    private final RegisterPushSubscriptionServiceCase registerPushSubscriptionServiceCase;
    private final UserInfoDTO userDetails;

    public RegisterPush(RegisterPushSubscriptionServiceCase registerPushSubscriptionServiceCase, UserInfoDTO userDetails) {
        this.registerPushSubscriptionServiceCase = registerPushSubscriptionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> registerNotificationSetupSecured(PushSubscriptionRequestDTO body) {
        registerPushSubscriptionServiceCase.registerPushSubscription(
                new RegisterPushSubscriptionCommand(body.getEndpoint(), body.getAuth(), body.getP256dh()),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
