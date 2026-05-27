package com.k9x.infrastructure.in.rest.endpoints.secured.users;


import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredUserFetchApiDelegate;
import com.k9x.oas.stub.model.UserProfileResponseDTO;
import org.springframework.http.ResponseEntity;

public class GetUserData implements SecuredUserFetchApiDelegate {

    private final UserInfoDTO userDetails;

    public GetUserData(UserInfoDTO userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<UserProfileResponseDTO> fetchUserDataSecured() {
        return ResponseEntity.ok(new UserProfileResponseDTO(userDetails.getEmail().split("@")[0], userDetails.getEmail(), userDetails.isOrganizer()));
    }
}
