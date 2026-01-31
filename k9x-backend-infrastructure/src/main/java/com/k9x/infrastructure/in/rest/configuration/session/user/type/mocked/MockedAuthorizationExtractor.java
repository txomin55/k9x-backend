package com.k9x.infrastructure.in.rest.configuration.session.user.type.mocked;

import com.k9x.infrastructure.in.rest.configuration.session.user.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.dto.RequestUserDetails;

import java.util.List;

public class MockedAuthorizationExtractor implements AuthorizationExtractor {

    private final RequestUserDetails details;

    public MockedAuthorizationExtractor(String id, String fullName, List<String> organizations) {
        this.details = new RequestUserDetails(id, fullName, organizations);
    }

    @Override
    public RequestUserDetails getDataFromToken(String authorization) {
        return this.details;
    }
}
