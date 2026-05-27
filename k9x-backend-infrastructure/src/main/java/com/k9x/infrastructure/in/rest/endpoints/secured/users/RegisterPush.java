package com.k9x.infrastructure.in.rest.endpoints.secured.users;

import com.k9x.oas.stub.api.SecuredUserRegisterPushApiDelegate;
import com.k9x.oas.stub.model.PushSubscriptionRequestDTO;
import org.springframework.http.ResponseEntity;

public class RegisterPush implements SecuredUserRegisterPushApiDelegate {

    @Override
    public ResponseEntity<String> registerNotificationSetupSecured(PushSubscriptionRequestDTO pushSubscriptionRequestDTO) {
        return ResponseEntity.ok("MOCKED");
    }
}
