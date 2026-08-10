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
    COMPETITION_CANNOT_BE_UPDATED("error.competition_cannot_be_updated"),
    STAGE_NOT_FOUND("error.stage_not_found"),
    STAGE_ALREADY_DELETED("error.stage_already_deleted"),
    STAGE_CANNOT_BE_DELETED("error.stage_cannot_be_deleted"),
    STAGE_CANNOT_BE_UPDATED("error.stage_cannot_be_updated"),
    STAGE_EXPIRED("error.stage_expired"),
    STAGE_DATE_TO_BEFORE_DATE_FROM("error.stage_date_to_before_date_from"),
    STAGE_NOT_STARTED("error.stage_not_started"),
    ENROLLMENT_CLOSED("error.enrollment_closed"),
    ENROLLMENT_DEADLINE_AFTER_STAGE_START("error.enrollment_deadline_after_stage_start"),
    EVENT_NOT_FOUND("error.event_not_found"),
    EVENT_NOT_IN_STAGE("error.event_not_in_stage"),
    EVENT_ALREADY_DELETED("error.event_already_deleted"),
    EVENT_CANNOT_BE_CREATED("error.event_cannot_be_created"),
    EVENT_CANNOT_BE_DELETED("error.event_cannot_be_deleted"),
    EVENT_CANNOT_BE_UPDATED("error.event_cannot_be_updated"),
    DISCIPLINE_CONFIGURATION_MALFORMED("error.discipline_configuration_malformed"),
    EVENT_CONFIGURATION_ID_REQUIRED("error.event_configuration_id_required"),
    COMPETITOR_NOT_FOUND("error.competitor_not_found"),
    COMPETITOR_ALREADY_NOT_COMPETING("error.competitor_already_not_competing"),
    YELLOW_CARD_ALREADY_REGISTERED("error.yellow_card_already_registered"),
    RED_CARD_ALREADY_REGISTERED("error.red_card_already_registered"),
    COMPETITOR_DISQUALIFIED("error.competitor_disqualified"),
    COMPETITOR_NOT_COMPETING("error.competitor_not_competing"),
    SCORE_NOT_ALLOWED("error.score_not_allowed"),
    USER_NOT_COLLECTOR("error.user_not_collector"),
    COLLECTOR_NOT_FOUND("error.collector_not_found"),
    EXERCISE_CONFIGURATION_NOT_FOUND("error.exercise_configuration_not_found"),
    DISCIPLINE_NOT_FOUND("error.discipline_not_found"),
    BIH_NOT_ALLOWED_FOR_SEX("error.bih_not_allowed_for_sex"),
    NOT_ENOUGH_JUDGES_FOR_MID_AVG("error.not_enough_judges_for_mid_avg"),
    DOG_IDENTIFICATION_ALREADY_EXISTS("error.dog_identification_already_exists"),
    DOG_ORIGIN_ALREADY_EXISTS("error.dog_origin_already_exists"),
    DUPLICATE_JUDGE_IN_EVENT("error.duplicate_judge_in_event"),
    DUPLICATE_EXERCISE_IN_EVENT("error.duplicate_exercise_in_event"),
    DUPLICATE_DOG_IN_EVENT("error.duplicate_dog_in_event"),
    EXERCISE_JUDGE_NOT_FOUND("error.exercise_judge_not_found"),
    EXERCISE_JUDGE_REQUIRED("error.exercise_judge_required"),
    EXERCISE_JUDGE_NOT_ASSIGNED("error.exercise_judge_not_assigned"),
    SUBSCRIPTION_KIND_NOT_SUPPORTED("error.subscription_kind_not_supported"),
    EVENT_FINISHED("error.event_finished"),
    STAGE_FINISHED("error.stage_finished"),
    NOTIFICATION_EVENTS_REQUIRED("error.notification_events_required"),
    RANKING_NOT_FOUND("error.ranking_not_found"),
    RANKING_GROUP_BY_INVALID("error.ranking_group_by_invalid"),
    RANKING_INCLUDE_BY_INVALID("error.ranking_include_by_invalid"),
    RANKING_EVENTS_REQUIRED("error.ranking_events_required"),
    RANKING_DUPLICATE_EVENT("error.ranking_duplicate_event"),
    RANKING_EVENT_NOT_AVAILABLE("error.ranking_event_not_available"),
    RANKING_INCLUDED_COUNT_REQUIRED("error.ranking_included_count_required");

    private final String id;

    ErrorEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
