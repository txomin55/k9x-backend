-- Where an EXTRACTION competition came from: when its evidence was collected and from where.
--
-- k9x.competitions.source already tells API rows apart from the ones an external ETL loaded. This is
-- the detail behind those EXTRACTION rows: one per edition, written by the ETL from a hand-written
-- event_metadata.json that sits next to the raw material. Without it the provenance lives only in
-- whoever downloaded the sheets, and is lost as soon as they forget.
CREATE TABLE k9x.extraction_metadata
(
    extraction_id        VARCHAR(255) NOT NULL,
    competition_id       VARCHAR(255) NOT NULL,
    -- When the evidence was collected, epoch millis. NOT when the row was loaded: that is created_at.
    -- The two differ by design, often by months: a 2020 sheet can be collected in 2025 and loaded today.
    extraction_timestamp BIGINT       NOT NULL,
    source               VARCHAR(255) NOT NULL,
    -- A closed vocabulary plus comma-separated parameters, e.g. 'FEDERATION_PAGE,cpc' or
    -- 'PRIVATE_CONVERSATIONS,ORGANIZER'. The ETL is what joins them and it refuses parameters
    -- containing a comma, so the first token is always the type.
    type                 VARCHAR(255) NOT NULL,
    created_at           BIGINT       NOT NULL,
    CONSTRAINT extraction_metadata_pkey PRIMARY KEY (extraction_id),
    CONSTRAINT extraction_metadata_competition_fk
        FOREIGN KEY (competition_id) REFERENCES k9x.competitions (id)
);

-- One competition can be extracted more than once (a re-collection from a better source), so this is
-- not unique: it is the way to find every extraction of a given competition.
CREATE INDEX extraction_metadata_competition_idx ON k9x.extraction_metadata (competition_id);
