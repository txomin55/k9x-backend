-- =============================================================================
-- Cleanup script: wipe every score of a single event.
--
-- Given an event id, physically remove all its `obdx.event_scores` rows.
-- Scores have NO deleted_at of their own, so this is a hard delete. The event,
-- its judges, dogs, exercises and competitors are left untouched — only the
-- score rows are purged, letting the event be re-scored from scratch.
--
-- Set the event id ONCE below (v_event_id), then run the whole script. Runs as
-- a single PL/pgSQL block — works in the Supabase SQL editor.
--
-- Idempotent: safe to run repeatedly.
-- =============================================================================

DO
$$
    DECLARE
        -- >>> Set the event id here <<<
        v_event_id text := 'XXX';
    BEGIN
        -- event_scores: references event, judge and dog. Purge every score of the event.
        DELETE
        FROM obdx.event_scores
        WHERE event_id = v_event_id;
    END
$$;
