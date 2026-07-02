-- Collapse the two yellow card slots into a single one:
--   drop yellow_card_2, rename yellow_card_1 -> yellow_card
ALTER TABLE obdx.event_scores
    DROP COLUMN yellow_card_2;
ALTER TABLE obdx.event_scores
    RENAME COLUMN yellow_card_1 TO yellow_card;
