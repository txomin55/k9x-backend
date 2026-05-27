package com.k9x.domain.exceptions.error;

public enum ErrorEnum {

    UNAUTHORIZED_RESOURCE_ERROR("error.unauthorized_resource"),
    NO_OWNER_WHEN_NO_ORGANIZER("error.no_owner_when_no_organizer"),
    JUDGE_NOT_FOUND("error.judge_not_found"),
    JUDGE_ALREADY_DELETED("error.judge_already_deleted"),
    DOG_NOT_FOUND("error.dog_not_found"),
    DOG_ALREADY_DELETED("error.dog_already_deleted"),
    COMPETITION_NOT_FOUND("error.competition_not_found"),
    COMPETITION_ALREADY_DELETED("error.competition_already_deleted"),
    STAGE_NOT_FOUND("error.stage_not_found"),
    STAGE_ALREADY_DELETED("error.stage_already_deleted"),
    STAGE_EXPIRED("error.stage_expired"),
    EVENT_NOT_FOUND("error.event_not_found"),
    EVENT_ALREADY_DELETED("error.event_already_deleted"),
    DISCIPLINE_CONFIGURATION_MALFORMED("error.discipline_configuration_malformed"),
    EVENT_CONFIGURATION_ID_REQUIRED("error.event_configuration_id_required");

    private final String id;

    ErrorEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
