package com.k9x.infrastructure.in.rest.endpoints.secured.users;

import com.k9x.application.users.use_case.RemovePushSubscriptionServiceCase;
import com.k9x.application.users.use_case.command.RemovePushSubscriptionCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredUserRemovePushApiDelegate;
import com.k9x.oas.stub.model.PushUnsubscribeRequestDTO;
import org.springframework.http.ResponseEntity;

public class RemovePush implements SecuredUserRemovePushApiDelegate {

    private final RemovePushSubscriptionServiceCase removePushSubscriptionServiceCase;
    private final UserInfoDTO userDetails;

    public RemovePush(RemovePushSubscriptionServiceCase removePushSubscriptionServiceCase, UserInfoDTO userDetails) {
        this.removePushSubscriptionServiceCase = removePushSubscriptionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeNotificationSetupSecured(PushUnsubscribeRequestDTO body) {
        removePushSubscriptionServiceCase.removePushSubscription(
                new RemovePushSubscriptionCommand(body.getEndpoint()),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
