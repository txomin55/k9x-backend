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

CREATE TABLE k9x.dogs
(
    id          VARCHAR(255) NOT NULL,
    identity    VARCHAR(255) NOT NULL,
    breed       VARCHAR(50)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    image       VARCHAR(255),
    owner       VARCHAR(50)  NOT NULL,
    creator     VARCHAR(50)  NOT NULL,
    country     VARCHAR(50)  NOT NULL,
    team        VARCHAR(50)  NOT NULL,
    last_update BIGINT       NOT NULL,
    created_at  BIGINT       NOT NULL,
    deleted_at  BIGINT,
    CONSTRAINT dogs_pkey PRIMARY KEY (id)
);

CREATE TABLE k9x.judges
(
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    creator     VARCHAR(50)  NOT NULL,
    last_update BIGINT       NOT NULL,
    created_at  BIGINT       NOT NULL,
    deleted_at  BIGINT,
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
    CONSTRAINT k9x_events_pkey PRIMARY KEY (id),
    CONSTRAINT k9x_events_fk FOREIGN KEY (stage_id) REFERENCES k9x.stages (id)
);

CREATE SCHEMA obdx;
CREATE TABLE obdx.event_competitors
(
    event_id      VARCHAR(255) NOT NULL,
    dog_id        VARCHAR(255) NOT NULL,
    position      SMALLINT,
    verified      BOOLEAN,
    last_update   BIGINT       NOT NULL,
    not_competing BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT obdx_event_competitors_pkey PRIMARY KEY (event_id, dog_id),
    CONSTRAINT obdx_event_competitors_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT obdx_event_competitors_dogs_fk FOREIGN KEY (dog_id) REFERENCES k9x.dogs (id)
);

CREATE TABLE obdx.event_judges
(
    event_id     VARCHAR(255) NOT NULL,
    judge_id     VARCHAR(255) NOT NULL,
    collector_id VARCHAR(255),
    ring         SMALLINT,
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
    last_update BIGINT       NOT NULL,
    CONSTRAINT obdx_event_exercises_pkey PRIMARY KEY (event_id, exercise_id),
    CONSTRAINT obdx_event_exercises_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id)
);

CREATE TABLE obdx.event_scores
(
    event_id    VARCHAR(255) NOT NULL,
    exercise_id VARCHAR(255) NOT NULL,
    judge_id    VARCHAR(255) NOT NULL,
    dog_id      VARCHAR(255) NOT NULL,
    score       NUMERIC(3, 1),
    created_at  BIGINT       NOT NULL,
    last_update BIGINT       NOT NULL,
    CONSTRAINT obdx_event_scores_pkey PRIMARY KEY (event_id, exercise_id, judge_id, dog_id),
    CONSTRAINT obdx_event_scores_event_fk FOREIGN KEY (event_id) REFERENCES k9x.events (id),
    CONSTRAINT obdx_event_scores_judge_fk FOREIGN KEY (judge_id) REFERENCES k9x.judges (id),
    CONSTRAINT obdx_event_scores_dog_fk FOREIGN KEY (dog_id) REFERENCES k9x.dogs (id)
)