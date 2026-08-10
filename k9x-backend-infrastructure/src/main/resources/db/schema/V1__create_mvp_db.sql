CREATE SCHEMA k9x;
CREATE TABLE k9x.users
(
    id    VARCHAR(255) NOT NULL,
    email VARCHAR(50)  NOT NULL,
    image VARCHAR(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE k9x.organizers
(
    user_id VARCHAR(255) NOT NULL,
    name    VARCHAR(255) NOT NULL,
    CONSTRAINT organizers_pkey PRIMARY KEY (user_id),
    CONSTRAINT organizers_user_fk FOREIGN KEY (user_id) REFERENCES k9x.users (id)
);

CREATE TABLE k9x.push_subscriptions
(
    endpoint    VARCHAR(500) NOT NULL,
    user_id     VARCHAR(255) NOT NULL,
    auth        VARCHAR(255) NOT NULL,
    p256dh      VARCHAR(255) NOT NULL,
    created_at  BIGINT       NOT NULL,
    last_update BIGINT       NOT NULL,
    CONSTRAINT push_subscriptions_pkey PRIMARY KEY (endpoint),
    CONSTRAINT push_subscriptions_user_fk FOREIGN KEY (user_id) REFERENCES k9x.users (id)
);

CREATE TABLE k9x.dogs
(
    identification                  VARCHAR(255) NOT NULL,
    origin                          VARCHAR(255) NOT NULL,
    license                         VARCHAR(255),
    breed                           VARCHAR(50)  NOT NULL,
    name                            VARCHAR(255) NOT NULL,
    image                           VARCHAR(255),
    owner                           VARCHAR(50),
    creator                         VARCHAR(50)  NOT NULL,
    country                         VARCHAR(50)  NOT NULL,
    team                            VARCHAR(50)  NOT NULL,
    last_update                     BIGINT       NOT NULL,
    created_at                      BIGINT       NOT NULL,
    deleted_at                      BIGINT,
    handler                         VARCHAR(255),
    sex                             VARCHAR(10),
    withers_cm                      INTEGER,
    three_fci_generations_confirmed BOOLEAN,
    CONSTRAINT dogs_pkey PRIMARY KEY (identification),
    CONSTRAINT k9x_dogs_sex_check
        CHECK (sex IS NULL OR sex IN ('MALE', 'FEMALE'))
);

CREATE TABLE k9x.judges
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    creator     VARCHAR(50)  NOT NULL,
    last_update BIGINT       NOT NULL,
    created_at  BIGINT       NOT NULL,
    deleted_at  BIGINT,
    country     VARCHAR(50)  NOT NULL DEFAULT '',
    CONSTRAINT judges_pkey PRIMARY KEY (id)
);

CREATE TABLE k9x.competitions
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    country     VARCHAR(50),
    description VARCHAR(255),
    address     VARCHAR(255),
    coord_alt   DOUBLE PRECISION,
    coord_long  DOUBLE PRECISION,
    creator     VARCHAR(50)  NOT NULL,
    last_update BIGINT       NOT NULL,
    created_at  BIGINT       NOT NULL,
    deleted_at  BIGINT,
    CONSTRAINT competitions_pkey PRIMARY KEY (id)
);

CREATE TABLE k9x.stages
(
    id             VARCHAR(255) NOT NULL,
    name           VARCHAR(255) NOT NULL,
    competition_id VARCHAR(255) NOT NULL,
    date_from      BIGINT       NOT NULL,
    date_to        BIGINT       NOT NULL,
    creator        VARCHAR(50)  NOT NULL,
    last_update    BIGINT       NOT NULL,
    created_at     BIGINT       NOT NULL,
    deleted_at     BIGINT,
    CONSTRAINT stages_pkey PRIMARY KEY (id),
    CONSTRAINT stages_organization_fk FOREIGN KEY (competition_id) REFERENCES k9x.competitions (id)
);

CREATE TABLE k9x.events
(
    id                  VARCHAR(255) NOT NULL,
    discipline          VARCHAR(50),
    configuration_id    VARCHAR(50),
    score_calculation   VARCHAR(10)  NOT NULL DEFAULT 'AVG',
    name                VARCHAR(255) NOT NULL,
    creator             VARCHAR(50)  NOT NULL,
    stage_id            VARCHAR(255) NOT NULL,
    enrollment_deadline BIGINT,
    last_update         BIGINT       NOT NULL,
    created_at          BIGINT       NOT NULL,
    deleted_at          BIGINT,
    awards              VARCHAR(50)[],
    rank_score          INTEGER,
    international       BOOLEAN,
    CONSTRAINT k9x_events_pkey PRIMARY KEY (id),
    CONSTRAINT k9x_events_fk FOREIGN KEY (stage_id) REFERENCES k9x.stages (id)
);

CREATE TABLE k9x.snap_dog_rank
(
    dog_identification VARCHAR(255) NOT NULL,
    discipline         VARCHAR(50)  NOT NULL,
    event_id           VARCHAR(255) NOT NULL,
    rank               NUMERIC(6, 2) NOT NULL,
    timestamp          BIGINT       NOT NULL,
    applying_timestamp BIGINT       NOT NULL,
    CONSTRAINT snap_dog_rank_pkey PRIMARY KEY (dog_identification, discipline, event_id),
    CONSTRAINT snap_dog_rank_dog_fk FOREIGN KEY (dog_identification) REFERENCES k9x.dogs (identification),
    CONSTRAINT snap_dog_rank_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id)
);

CREATE TABLE k9x.snap_dog_index_history
(
    dog_identification VARCHAR(255) NOT NULL,
    discipline         VARCHAR(50)  NOT NULL,
    rank               INTEGER      NOT NULL,
    timestamp          BIGINT       NOT NULL,
    applying_timestamp BIGINT       NOT NULL,
    metadata           TEXT         NOT NULL,
    CONSTRAINT snap_dog_index_history_pkey PRIMARY KEY (dog_identification, discipline, applying_timestamp),
    CONSTRAINT snap_dog_index_history_dog_fk FOREIGN KEY (dog_identification) REFERENCES k9x.dogs (identification),
    CONSTRAINT k9x_snap_dog_index_history_rank_check
        CHECK (rank BETWEEN 0 AND 1000)
);

CREATE SCHEMA obdx;
CREATE TABLE obdx.event_competitors
(
    event_id           VARCHAR(255) NOT NULL,
    dog_identification VARCHAR(255) NOT NULL,
    start_number       SMALLINT,
    competitor_number  SMALLINT,
    verified           BOOLEAN,
    last_update        BIGINT       NOT NULL,
    not_competing      BOOLEAN      NOT NULL DEFAULT FALSE,
    bih                BOOLEAN,
    primer             VARCHAR(255),
    reserve            BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT obdx_event_competitors_pkey PRIMARY KEY (event_id, dog_identification),
    CONSTRAINT obdx_event_competitors_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT obdx_event_competitors_dogs_fk FOREIGN KEY (dog_identification) REFERENCES k9x.dogs (identification)
);

CREATE TABLE obdx.snap_event_competitors_results
(
    event_id           VARCHAR(255) NOT NULL,
    dog_identification VARCHAR(255) NOT NULL,
    position           SMALLINT,
    total_score        NUMERIC(6, 2),
    rank_score         NUMERIC(6, 2),
    timestamp          BIGINT       NOT NULL,
    applying_timestamp BIGINT       NOT NULL,
    CONSTRAINT snap_event_competitors_results_pkey PRIMARY KEY (event_id, dog_identification),
    CONSTRAINT snap_event_competitors_results_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT snap_event_competitors_results_dog_fk FOREIGN KEY (dog_identification) REFERENCES k9x.dogs (identification)
);

CREATE TABLE obdx.event_judges
(
    event_id     VARCHAR(255) NOT NULL,
    judge_id     VARCHAR(255) NOT NULL,
    collector_id VARCHAR(255),
    last_update  BIGINT       NOT NULL,
    CONSTRAINT obdx_event_judges_pkey PRIMARY KEY (event_id, judge_id),
    CONSTRAINT obdx_event_judges_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT obdx_event_judges_judge_fk FOREIGN KEY (judge_id) REFERENCES k9x.judges (id),
    CONSTRAINT obdx_event_judges_users_fk FOREIGN KEY (collector_id) REFERENCES k9x.users (id)
);

CREATE TABLE obdx.event_exercises
(
    event_id    VARCHAR(255) NOT NULL,
    exercise_id VARCHAR(255) NOT NULL,
    position    SMALLINT     NOT NULL,
    tags        VARCHAR(50)[],
    judges      VARCHAR(255)[],
    last_update BIGINT       NOT NULL,
    CONSTRAINT obdx_event_exercises_pkey PRIMARY KEY (event_id, exercise_id),
    CONSTRAINT obdx_event_exercises_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id)
);

CREATE TABLE obdx.event_scores
(
    event_id           VARCHAR(255) NOT NULL,
    exercise_id        VARCHAR(255) NOT NULL,
    judge_id           VARCHAR(255) NOT NULL,
    dog_identification VARCHAR(255) NOT NULL,
    score              NUMERIC(3, 1),
    created_at         BIGINT       NOT NULL,
    last_update        BIGINT       NOT NULL,
    yellow_card        BIGINT,
    red_card           BIGINT,
    CONSTRAINT obdx_event_scores_pkey PRIMARY KEY (event_id, exercise_id, judge_id, dog_identification),
    CONSTRAINT obdx_event_scores_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT obdx_event_scores_judge_fk FOREIGN KEY (judge_id) REFERENCES k9x.judges (id),
    CONSTRAINT obdx_event_scores_dog_fk FOREIGN KEY (dog_identification) REFERENCES k9x.dogs (identification)
);

CREATE TABLE obdx.snap_event_classification
(
    event_id           VARCHAR(255) NOT NULL,
    timestamp          BIGINT       NOT NULL,
    applying_timestamp BIGINT       NOT NULL,
    snapshot           JSON         NOT NULL,
    CONSTRAINT snap_event_classification_pkey PRIMARY KEY (event_id),
    CONSTRAINT snap_event_classification_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id)
);

CREATE TABLE k9x.notifications
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    user_id    VARCHAR(255) NOT NULL,
    event_type VARCHAR(50)  NOT NULL,
    metadata   TEXT         NOT NULL,
    created_at BIGINT       NOT NULL,
    seen       BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT notifications_pkey PRIMARY KEY (id),
    CONSTRAINT notifications_user_fk FOREIGN KEY (user_id) REFERENCES k9x.users (id)
);

CREATE TABLE k9x.event_notifications
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY,
    timestamp BIGINT NOT NULL,
    content   TEXT   NOT NULL,
    CONSTRAINT event_notifications_pkey PRIMARY KEY (id)
);

CREATE TABLE k9x.events_event_notifications
(
    event_id              VARCHAR(255) NOT NULL,
    event_notification_id BIGINT       NOT NULL,
    CONSTRAINT events_event_notifications_pkey PRIMARY KEY (event_id, event_notification_id),
    CONSTRAINT events_event_notifications_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT events_event_notifications_notification_fk FOREIGN KEY (event_notification_id) REFERENCES k9x.event_notifications (id)
);

CREATE TABLE k9x.user_subscriptions
(
    user_id   VARCHAR(255)       NOT NULL,
    event_ids VARCHAR(255) ARRAY NOT NULL,
    CONSTRAINT user_subscriptions_pkey PRIMARY KEY (user_id),
    CONSTRAINT user_subscriptions_user_fk FOREIGN KEY (user_id) REFERENCES k9x.users (id)
);

CREATE TABLE k9x.rankings
(
    id             VARCHAR(255)       NOT NULL,
    name           VARCHAR(255)       NOT NULL,
    event_ids      VARCHAR(255) ARRAY NOT NULL,
    group_by       VARCHAR(50)        NOT NULL,
    include_by     VARCHAR(50)        NOT NULL,
    included_count INTEGER,
    include_reserves BOOLEAN            NOT NULL DEFAULT TRUE,
    creator        VARCHAR(50)        NOT NULL,
    created_at     BIGINT             NOT NULL,
    CONSTRAINT rankings_pkey PRIMARY KEY (id)
);

CREATE INDEX rankings_creator_idx ON k9x.rankings (creator);
