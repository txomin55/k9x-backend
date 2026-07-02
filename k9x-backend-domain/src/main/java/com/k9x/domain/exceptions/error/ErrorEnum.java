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
    COMPETITION_CANNOT_BE_DELETED("error.competition_cannot_be_deleted"),
    STAGE_NOT_FOUND("error.stage_not_found"),
    STAGE_ALREADY_DELETED("error.stage_already_deleted"),
    STAGE_CANNOT_BE_DELETED("error.stage_cannot_be_deleted"),
    STAGE_EXPIRED("error.stage_expired"),
    STAGE_NOT_STARTED("error.stage_not_started"),
    EVENT_NOT_FOUND("error.event_not_found"),
    EVENT_ALREADY_DELETED("error.event_already_deleted"),
    EVENT_CANNOT_BE_DELETED("error.event_cannot_be_deleted"),
    DISCIPLINE_CONFIGURATION_MALFORMED("error.discipline_configuration_malformed"),
    EVENT_CONFIGURATION_ID_REQUIRED("error.event_configuration_id_required"),
    COMPETITOR_NOT_FOUND("error.competitor_not_found"),
    COMPETITOR_ALREADY_NOT_COMPETING("error.competitor_already_not_competing"),
    YELLOW_CARD_ALREADY_REGISTERED("error.yellow_card_already_registered"),
    COMPETITOR_DISQUALIFIED("error.competitor_disqualified"),
    SCORE_NOT_ALLOWED("error.score_not_allowed"),
    USER_NOT_COLLECTOR("error.user_not_collector"),
    COLLECTOR_NOT_FOUND("error.collector_not_found"),
    EXERCISE_CONFIGURATION_NOT_FOUND("error.exercise_configuration_not_found");

    private final String id;

    ErrorEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
