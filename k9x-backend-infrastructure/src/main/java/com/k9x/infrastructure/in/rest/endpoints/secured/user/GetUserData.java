package com.k9x.infrastructure.in.rest.endpoints.secured.user;


import com.k9x.application.users.dto.AuthTokenDTO;
import com.k9x.oas.stub.api.SecuredUserFetchApiDelegate;
import com.k9x.oas.stub.model.UserProfileResponseDTO;
import org.springframework.http.ResponseEntity;

public class GetUserData implements SecuredUserFetchApiDelegate {

    private final AuthTokenDTO userDetails;

    public GetUserData(AuthTokenDTO userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<UserProfileResponseDTO> fetchUserDataSecured() {
        return ResponseEntity.ok(new UserProfileResponseDTO(userDetails.getSubject().split("@")[0], userDetails.getSubject(), true));
    }
}
