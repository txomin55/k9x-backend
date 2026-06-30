-- =============================================================================
-- Cleanup script: hard-delete every soft-deleted entity and all the join/child
-- records that reference it.
--
-- The aggregates (competitions, stages, events, dogs, judges) use soft deletes
-- (deleted_at IS NOT NULL). This script purges them physically along with their
-- dangling join rows in the `obdx` schema (event_competitors, event_exercises,
-- event_judges, event_scores), which have NO deleted_at of their own.
--
-- Deletion cascades DOWN the competition -> stage -> event hierarchy:
--   * a deleted competition purges all its stages and their events
--   * a deleted stage purges all its events
--   * a deleted event purges its join rows
-- Soft-deleted dogs and judges are purged too, together with any join row that
-- references them.
--
-- Rows are removed in foreign-key order: join tables first, then events, stages,
-- competitions, and finally dogs / judges.
--
-- Idempotent: safe to run repeatedly. Wrapped in a single transaction.
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- Resolve the set of entities to purge (own soft-delete or via a deleted parent).
-- ---------------------------------------------------------------------------

-- Soft-deleted competitions.
CREATE TEMP TABLE _deleted_competitions ON COMMIT DROP AS
SELECT id FROM k9x.competitions WHERE deleted_at IS NOT NULL;

-- Stages soft-deleted, or belonging to a deleted competition.
CREATE TEMP TABLE _deleted_stages ON COMMIT DROP AS
SELECT s.id
FROM k9x.stages s
WHERE s.deleted_at IS NOT NULL
   OR s.competition_id IN (SELECT id FROM _deleted_competitions);

-- Events soft-deleted, or belonging to a deleted stage.
CREATE TEMP TABLE _deleted_events ON COMMIT DROP AS
SELECT e.id
FROM k9x.events e
WHERE e.deleted_at IS NOT NULL
   OR e.stage_id IN (SELECT id FROM _deleted_stages);

-- Soft-deleted dogs.
CREATE TEMP TABLE _deleted_dogs ON COMMIT DROP AS
SELECT id FROM k9x.dogs WHERE deleted_at IS NOT NULL;

-- Soft-deleted judges.
CREATE TEMP TABLE _deleted_judges ON COMMIT DROP AS
SELECT id FROM k9x.judges WHERE deleted_at IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 1) Join tables (obdx) — must go first, they FK into events / dogs / judges.
-- ---------------------------------------------------------------------------

-- event_scores: references event, judge and dog.
DELETE FROM obdx.event_scores
WHERE event_id IN (SELECT id FROM _deleted_events)
   OR judge_id IN (SELECT id FROM _deleted_judges)
   OR dog_id IN (SELECT id FROM _deleted_dogs);

-- event_exercises: references event only.
DELETE FROM obdx.event_exercises
WHERE event_id IN (SELECT id FROM _deleted_events);

-- event_judges: references event and judge.
DELETE FROM obdx.event_judges
WHERE event_id IN (SELECT id FROM _deleted_events)
   OR judge_id IN (SELECT id FROM _deleted_judges);

-- event_competitors: references event and dog.
DELETE FROM obdx.event_competitors
WHERE event_id IN (SELECT id FROM _deleted_events)
   OR dog_id IN (SELECT id FROM _deleted_dogs);

-- ---------------------------------------------------------------------------
-- 2) Aggregates — in FK order: events -> stages -> competitions, then dogs / judges.
-- ---------------------------------------------------------------------------

DELETE FROM k9x.events WHERE id IN (SELECT id FROM _deleted_events);
DELETE FROM k9x.stages WHERE id IN (SELECT id FROM _deleted_stages);
DELETE FROM k9x.competitions WHERE id IN (SELECT id FROM _deleted_competitions);

DELETE FROM k9x.dogs WHERE id IN (SELECT id FROM _deleted_dogs);
DELETE FROM k9x.judges WHERE id IN (SELECT id FROM _deleted_judges);

COMMIT;