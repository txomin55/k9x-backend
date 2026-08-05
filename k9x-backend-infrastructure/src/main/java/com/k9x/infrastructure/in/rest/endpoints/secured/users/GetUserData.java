package com.k9x.infrastructure.in.rest.endpoints.secured.users;


import com.k9x.application.subscriptions.use_case.GetUserSubscriptionsServiceCase;
import com.k9x.application.subscriptions.use_case.dto.UserSubscriptionsDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredUserFetchApiDelegate;
import com.k9x.oas.stub.model.UserProfileResponseDTO;
import com.k9x.oas.stub.model.UserSubscriptionsResponseDTO;
import org.springframework.http.ResponseEntity;

public class GetUserData implements SecuredUserFetchApiDelegate {

    private final GetUserSubscriptionsServiceCase getUserSubscriptionsServiceCase;
    private final UserInfoDTO userDetails;

    public GetUserData(GetUserSubscriptionsServiceCase getUserSubscriptionsServiceCase, UserInfoDTO userDetails) {
        this.getUserSubscriptionsServiceCase = getUserSubscriptionsServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<UserProfileResponseDTO> fetchUserDataSecured() {
        UserSubscriptionsDTO subscriptions = getUserSubscriptionsServiceCase.getUserSubscriptions(userDetails.getEmail());
        return ResponseEntity.ok(new UserProfileResponseDTO(
                userDetails.getEmail().split("@")[0],
                userDetails.getEmail(),
                userDetails.getImage(),
                userDetails.isOrganizer(),
                new UserSubscriptionsResponseDTO(subscriptions.eventIds())));
    }
}
