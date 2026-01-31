package com.k9x.domain.commons.exception.error;

public enum ErrorEnum {

    UNAUTHORIZED_RESOURCE_ERROR("error.unauthorized_resource"),
    UNAUTHORIZED_RESOURCE_STATE_TRANSITION_ERROR("error.unauthorized_resource_state_transition");

    private final String id;

    ErrorEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
