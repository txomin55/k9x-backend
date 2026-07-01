-- Add two competitor result fields to obdx.event_competitors:
--   bih         -> boolean flag
--   final_score -> numeric score with 2 decimals, nullable, ranging from 0 to 1000
ALTER TABLE obdx.event_competitors
    ADD COLUMN bih         BOOLEAN,
    ADD COLUMN final_score NUMERIC(6, 2),
    ADD CONSTRAINT obdx_event_competitors_final_score_range
        CHECK (final_score IS NULL OR (final_score >= 0 AND final_score <= 1000));