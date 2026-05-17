package com.k9x.domain.exceptions.error;

public enum ErrorEnum {

    UNAUTHORIZED_RESOURCE_ERROR("error.unauthorized_resource"),
    NO_OWNER_WHEN_NO_ORGANIZER("error.no_owner_when_no_organizer"),
    JUDGE_NOT_FOUND("error.judge_not_found"),
    JUDGE_ALREADY_DELETED("error.judge_already_deleted"),
    DOG_NOT_FOUND("error.dog_not_found"),
    DOG_ALREADY_DELETED("error.dog_already_deleted"),
    COMPETITION_NOT_FOUND("error.competition_not_found"),
    COMPETITION_ALREADY_DELETED("error.competition_already_deleted");

    private final String id;

    ErrorEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
