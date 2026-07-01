-- Add two yellow card fields to obdx.event_scores:
--   yellow_card_1 -> nullable bigint
--   yellow_card_2 -> nullable bigint
ALTER TABLE obdx.event_scores
    ADD COLUMN yellow_card_1 BIGINT,
    ADD COLUMN yellow_card_2 BIGINT;
