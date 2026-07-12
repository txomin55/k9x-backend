-- =============================================================================
-- Cleanup script: hard-delete an entire competition tree.
--
-- Given a competition id, physically remove the competition, all its stages,
-- all their events, and every obdx join row that references those events
-- (event_scores, event_exercises, event_judges, event_competitors).
--
-- This is a HARD delete regardless of deleted_at — it purges active rows too.
-- Dogs and judges are NOT removed (they are shared across competitions); only
-- the join rows tying them to the purged events are dropped.
--
-- Rows are removed in foreign-key order: obdx join tables first, then events,
-- stages, and finally the competition.
--
-- Set the competition id ONCE below (v_competition_id), then run the whole
-- script. Runs as a single PL/pgSQL block — works in the Supabase SQL editor.
--
-- Idempotent: safe to run repeatedly.
-- =============================================================================

DO
$$
    DECLARE
        -- >>> Set the competition id here <<<
        v_competition_id text := 'XXXX';
    BEGIN
        -- 1) obdx join tables — must go first, they FK into events.
        DELETE
        FROM obdx.event_scores
        WHERE event_id IN (SELECT e.id
                           FROM k9x.events e
                                    JOIN k9x.stages s ON e.stage_id = s.id
                           WHERE s.competition_id = v_competition_id);

        DELETE
        FROM obdx.event_exercises
        WHERE event_id IN (SELECT e.id
                           FROM k9x.events e
                                    JOIN k9x.stages s ON e.stage_id = s.id
                           WHERE s.competition_id = v_competition_id);

        DELETE
        FROM obdx.event_judges
        WHERE event_id IN (SELECT e.id
                           FROM k9x.events e
                                    JOIN k9x.stages s ON e.stage_id = s.id
                           WHERE s.competition_id = v_competition_id);

        DELETE
        FROM obdx.event_competitors
        WHERE event_id IN (SELECT e.id
                           FROM k9x.events e
                                    JOIN k9x.stages s ON e.stage_id = s.id
                           WHERE s.competition_id = v_competition_id);

        -- 2) Aggregates — in FK order: events -> stages -> competition.
        DELETE
        FROM k9x.events
        WHERE stage_id IN (SELECT id
                           FROM k9x.stages
                           WHERE competition_id = v_competition_id);

        DELETE
        FROM k9x.stages
        WHERE competition_id = v_competition_id;

        DELETE
        FROM k9x.competitions
        WHERE id = v_competition_id;
    END
$$;
