package com.k9x.infrastructure.in.rest.configuration.session.user.dto;

import java.util.List;

public record RequestUserDetails(
        String id,
        String fullName,
        List<String> organizations
) {
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getOrganizations() {
        return organizations;
    }
}
