package com.k9x.infrastructure.in.rest.endpoints.user;


import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.oas.stub.api.GetUserDataApiDelegate;
import com.k9x.oas.stub.model.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetUserData implements GetUserDataApiDelegate {

    private final AuthTokenDTO userDetails;

    public GetUserData(AuthTokenDTO userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<UserDetails> getUserData() {
        return ResponseEntity.ok(new UserDetails(userDetails.getSubject(), List.of()));
    }
}
