package com.k9x.domain.exceptions.error;

public enum ErrorEnum {

    UNAUTHORIZED_RESOURCE_ERROR("error.unauthorized_resource"),
    NO_OWNER_WHEN_NO_ORGANIZER("error.no_owner_when_no_organizer");

    private final String id;

    ErrorEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
