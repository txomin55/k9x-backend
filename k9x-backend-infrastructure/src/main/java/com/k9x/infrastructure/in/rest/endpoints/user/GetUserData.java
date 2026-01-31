package com.k9x.infrastructure.in.rest.endpoints.user;


import com.k9x.oas.stub.api.GetUserDataApiDelegate;
import com.k9x.infrastructure.in.rest.configuration.session.user.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.dto.RequestUserDetails;
import com.k9x.oas.stub.model.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GetUserData implements GetUserDataApiDelegate {

    private final AuthorizationExtractor tokenExtractor;

    public GetUserData(AuthorizationExtractor tokenExtractor) {
        this.tokenExtractor = tokenExtractor;
    }

    @Override
    public ResponseEntity<UserDetails> getUserData(String authorization) {
        RequestUserDetails user = tokenExtractor.getDataFromToken(authorization);
        return ResponseEntity.ok(new UserDetails(user.getId(), user.getOrganizations()));
    }
}
