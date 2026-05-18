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
    competition_id VARCHAR(50)  NOT NULL,
    date_from      BIGINT       NOT NULL,
    date_to        BIGINT       NOT NULL,
    creator        VARCHAR(50)  NOT NULL,
    last_update    BIGINT       NOT NULL,
    created_at     BIGINT       NOT NULL,
    deleted_at     BIGINT,
    CONSTRAINT stages_pkey PRIMARY KEY (id),
    CONSTRAINT stages_organization_fk FOREIGN KEY (competition_id) REFERENCES k9x.competitions (id)

);
