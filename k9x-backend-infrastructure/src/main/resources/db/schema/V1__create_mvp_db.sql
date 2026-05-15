CREATE SCHEMA k9x;

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
    deleted_at  BIGINT       NOT NULL,
    CONSTRAINT dogs_pkey PRIMARY KEY (id)
);
