-- Add the red card field to obdx.event_scores: nullable bigint, only one can ever be stamped per dog
ALTER TABLE obdx.event_scores
    ADD COLUMN red_card BIGINT;
