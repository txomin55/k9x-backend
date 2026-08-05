package com.k9x.infrastructure.in.rest.endpoints.secured.subscriptions;

import com.k9x.application.subscriptions.use_case.UpdateUserSubscriptionServiceCase;
import com.k9x.application.subscriptions.use_case.command.UpdateUserSubscriptionCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredSubscriptionsUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateSubscriptionRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateSubscription implements SecuredSubscriptionsUpdateApiDelegate {

    private final UpdateUserSubscriptionServiceCase updateUserSubscriptionServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateSubscription(UpdateUserSubscriptionServiceCase updateUserSubscriptionServiceCase,
            UserInfoDTO userDetails) {
        this.updateUserSubscriptionServiceCase = updateUserSubscriptionServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<Void> updateSubscriptionSecured(UpdateSubscriptionRequestDTO body) {
        updateUserSubscriptionServiceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand(body.getKind(), body.getIds(), Boolean.TRUE.equals(body.getSubscribe())),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
