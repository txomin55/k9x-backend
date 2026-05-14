CREATE TABLE dogs (
    id          VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    image       VARCHAR(255),
    owner       VARCHAR(255) NOT NULL,
    state       VARCHAR(50)  NOT NULL,
    last_update BIGINT       NOT NULL,
    CONSTRAINT dogs_pkey PRIMARY KEY (id)
);
