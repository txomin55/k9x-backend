-- =============================================================================
-- 20º Trofeu AVSC: obdx schema data (event judges, event exercises, event
-- competitors), sourced from competition 'competition_1783770154788',
-- proba.jpeg and the OBDX configuration.json files.
--
-- This is a one-off data script, NOT a Flyway migration/seed: run it manually
-- against a target database after the schema/seed migrations have been applied.
-- Run this file AFTER trofeu_avsc_k9x.sql, whose rows it references via FK
-- (events, judges and dogs must already exist).
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 1) Event judges (Ivan Ramil judges every event)
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_judges (event_id, judge_id, collector_id, last_update)
VALUES ('event_1783770421009', 'judge_1783770129340', 'acsupercaes@gmail.com', 1783770147887),
       ('event_1783770430523', 'judge_1783770129340', 'acsupercaes@gmail.com', 1783770147887),
       ('event_1783770452508', 'judge_1783770129340', 'acsupercaes@gmail.com', 1783770147887),
       ('event_1783770408793', 'judge_1783770129340', 'acsupercaes@gmail.com', 1783770147887);

-- ---------------------------------------------------------------------------
-- 2) Event exercises: every exercise from each event's configuration, in the
--    configuration's declared order (position 1..N). tags NULL; judges is the
--    single event judge (Ivan Ramil). Sourced from the OBDX configuration.json
--    files under .../disciplines/obdx/federations/.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_exercises (event_id, exercise_id, position, tags, judges, last_update)
VALUES -- Classe 1 (OBDX_FCI_GRADE_1_V0)
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.1_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.2_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.3_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.4_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.5_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.6_V0', 6, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.7_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.8_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.9_V0', 9, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 2 (OBDX_FCI_GRADE_2_V0)
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.1_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.2_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.3_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.4_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.5_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.6_V0', 6, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.7_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.8_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.9_V0', 9, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.10_V0', 10, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 3 (OBDX_FCI_GRADE_3.V0)
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.1_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.2_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.3_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.4_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.5_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.6_V0', 6, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.7_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.8_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.9_V0', 9, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.10_V0', 10, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- COBS (CPC_COBS_V0)
       ('event_1783770408793', 'OBDX_CPC_COBS.1_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.2_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.3_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.4_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.5_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.6_V0', 6, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.7_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.8_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000));

-- ---------------------------------------------------------------------------
-- 3) Event competitors: each dog enrolled in the event of its class
--    (Classe 1/2/3 and COBS). All verified; no results yet, so position is NULL.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, verified, last_update, not_competing, reserve)
VALUES -- Classe 1 -> event_1783770421009
       ('event_1783770421009', 'avsc-dog-1', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770421009', 'avsc-dog-2', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       -- Classe 2 -> event_1783770430523
       ('event_1783770430523', 'avsc-dog-3', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770430523', 'avsc-dog-4', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770430523', 'avsc-dog-5', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       -- Classe 3 -> event_1783770452508
       ('event_1783770452508', 'avsc-dog-6', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770452508', 'avsc-dog-7', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770452508', 'avsc-dog-8', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770452508', 'avsc-dog-9', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       -- COBS -> event_1783770408793
       ('event_1783770408793', 'avsc-dog-10', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770408793', 'avsc-dog-11', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770408793', 'avsc-dog-12', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770408793', 'avsc-dog-13', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770408793', 'avsc-dog-14', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE),
       ('event_1783770408793', 'avsc-dog-15', TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE);

COMMIT;
