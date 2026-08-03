-- =============================================================================
-- Cleanup script: hard-delete everything the smoke-test suite created.
--
-- The smoke tests (k9x-frontend ui/app/smoke) name everything they create with
-- the "--SMOKE--" prefix: competitions, stages, events, dogs and judges. This
-- script physically removes every row whose name carries that prefix, plus
-- everything hanging off those entities:
--
--   * k9x:  competitions -> stages -> events, dogs, judges, and the snapshot
--           tables referencing them (snap_dog_rank, snap_dog_index_history).
--   * obdx: every row referencing a smoke event, dog or judge (event_scores,
--           event_exercises, event_judges, event_competitors,
--           snap_event_competitors_results, snap_event_classification).
--
-- Stages/events are selected both by their own prefixed name AND by belonging
-- to a smoke competition, so a smoke tree is purged whole even if a child was
-- renamed. Non-smoke dogs that were enrolled into a smoke event only lose the
-- join rows (enrollment/scores) tying them to the purged event — the dogs
-- themselves are kept. Conversely, obdx rows tying a smoke dog/judge to a
-- NON-smoke event are also dropped, as the FK requires it for the hard delete.
--
-- This is a HARD delete regardless of deleted_at — it purges active rows too.
-- Rows are removed in foreign-key order: obdx/snapshot tables first, then
-- events, stages, competitions, dogs and judges.
--
-- Runs as a single PL/pgSQL block — works in the Supabase SQL editor.
-- Idempotent: safe to run repeatedly.
-- =============================================================================

DO
$$
    DECLARE
        v_prefix text := '--SMOKE--%';
    BEGIN
        DROP TABLE IF EXISTS smoke_competitions, smoke_stages, smoke_events,
            smoke_dogs, smoke_judges;

        CREATE TEMP TABLE smoke_competitions AS
        SELECT id
        FROM k9x.competitions
        WHERE name LIKE v_prefix;

        CREATE TEMP TABLE smoke_stages AS
        SELECT id
        FROM k9x.stages
        WHERE name LIKE v_prefix
           OR competition_id IN (SELECT id FROM smoke_competitions);

        CREATE TEMP TABLE smoke_events AS
        SELECT id
        FROM k9x.events
        WHERE name LIKE v_prefix
           OR stage_id IN (SELECT id FROM smoke_stages);

        CREATE TEMP TABLE smoke_dogs AS
        SELECT id
        FROM k9x.dogs
        WHERE name LIKE v_prefix;

        CREATE TEMP TABLE smoke_judges AS
        SELECT id
        FROM k9x.judges
        WHERE name LIKE v_prefix;

        -- 1) obdx tables — must go first, they FK into events/dogs/judges.
        DELETE
        FROM obdx.event_scores
        WHERE event_id IN (SELECT id FROM smoke_events)
           OR judge_id IN (SELECT id FROM smoke_judges)
           OR dog_id IN (SELECT id FROM smoke_dogs);

        DELETE
        FROM obdx.event_exercises
        WHERE event_id IN (SELECT id FROM smoke_events);

        DELETE
        FROM obdx.event_judges
        WHERE event_id IN (SELECT id FROM smoke_events)
           OR judge_id IN (SELECT id FROM smoke_judges);

        DELETE
        FROM obdx.event_competitors
        WHERE event_id IN (SELECT id FROM smoke_events)
           OR dog_id IN (SELECT id FROM smoke_dogs);

        DELETE
        FROM obdx.snap_event_competitors_results
        WHERE event_id IN (SELECT id FROM smoke_events)
           OR dog_id IN (SELECT id FROM smoke_dogs);

        DELETE
        FROM obdx.snap_event_classification
        WHERE event_id IN (SELECT id FROM smoke_events);

        -- 2) k9x snapshot tables referencing events/dogs.
        DELETE
        FROM k9x.snap_dog_rank
        WHERE event_id IN (SELECT id FROM smoke_events)
           OR dog_id IN (SELECT id FROM smoke_dogs);

        DELETE
        FROM k9x.snap_dog_index_history
        WHERE dog_id IN (SELECT id FROM smoke_dogs);

        -- 3) Aggregates — in FK order: events -> stages -> competitions.
        DELETE
        FROM k9x.events
        WHERE id IN (SELECT id FROM smoke_events);

        DELETE
        FROM k9x.stages
        WHERE id IN (SELECT id FROM smoke_stages);

        DELETE
        FROM k9x.competitions
        WHERE id IN (SELECT id FROM smoke_competitions);

        -- 4) Standalone smoke entities.
        DELETE
        FROM k9x.dogs
        WHERE id IN (SELECT id FROM smoke_dogs);

        DELETE
        FROM k9x.judges
        WHERE id IN (SELECT id FROM smoke_judges);

        DROP TABLE smoke_competitions, smoke_stages, smoke_events, smoke_dogs,
            smoke_judges;
    END
$$;
