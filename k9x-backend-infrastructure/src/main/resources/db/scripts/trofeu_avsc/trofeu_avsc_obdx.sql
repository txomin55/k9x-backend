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
-- 2) Event exercises: every exercise from each event's configuration. tags NULL;
--    judges is the single event judge (Ivan Ramil). Exercise ids sourced from the
--    OBDX configuration.json files under .../disciplines/obdx/federations/.
--    position = the order the exercises were actually run at this event (from the
--    proba.jpeg scoresheet), which for Classe 3 differs from the configuration's
--    declared exercise numbering.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_exercises (event_id, exercise_id, position, tags, judges, last_update)
VALUES -- Classe 1 (OBDX_FCI_GRADE_1_V0)
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.1_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.2_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.3_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.4_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.5_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.6_V0', 6, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.7_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.8_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.9_V0', 9, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 2 (OBDX_FCI_GRADE_2_V0)
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.1_V0', 9, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.2_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.3_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.4_V0', 6, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.5_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.6_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.7_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.8_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.9_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.10_V0', 10, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 3 (OBDX_FCI_GRADE_3.V0)
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.1_V0', 9, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.2_V0', 10, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.3_V0', 2, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.4_V0', 3, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.5_V0', 5, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.6_V0', 8, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.7_V0', 4, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.8_V0', 1, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.9_V0', 7, NULL, ARRAY ['judge_1783770129340'],
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.10_V0', 6, NULL, ARRAY ['judge_1783770129340'],
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
--    (Classe 1/2/3 and COBS). All verified.
--    position = the competitor's dorsal number (bib), taken from the scoresheet
--    PDF. Competitors with no scoresheet (avsc-dog-2, avsc-dog-12) have an
--    unknown dorsal, so they are placed last within their event (next free
--    number after the known dorsals). final_score = the sheet's total "Média"
--    column; it is NULL when there is no scoresheet or the dog was disqualified
--    (avsc-dog-15, "Desqualificado").
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, position, verified, last_update, not_competing, reserve,
                                    final_score)
VALUES -- Classe 1 -> event_1783770421009
       ('event_1783770421009', '621XXXXXXXXXXXX', 1, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770421009', 'avsc-dog-2', 2, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       -- Classe 2 -> event_1783770430523
       ('event_1783770430523', '620095300051547', 3, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770430523', '622XXXXXXXXXXXX', 4, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770430523', '623XXXXXXXXXXXX', 5, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       -- Classe 3 -> event_1783770452508
       ('event_1783770452508', '380260102376914', 6, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770452508', '945000001842619', 7, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770452508', '978000040086211', 8, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770452508', '620098500124755', 9, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       -- COBS -> event_1783770408793 (dorsals 10, 11, 13, 14, 15; avsc-dog-12 has no
       -- scoresheet -> placed last as 16)
       ('event_1783770408793', '624XXXXXXXXXXXX', 10, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770408793', '963XXXXXXXXXXXX', 11, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770408793', '956XXXXXXXXXXXX', 13, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770408793', '992XXXXXXXXXXXX', 14, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770408793', '625XXXXXXXXXXXX', 15, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL),
       ('event_1783770408793', 'avsc-dog-12', 16, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, FALSE, NULL);

-- ---------------------------------------------------------------------------
-- 4) Event scores: the raw per-exercise score (the "Juiz 1" column) from each
--    dog's individual scoresheet PDF (FOLHA DE PONTUAÇÃO INDIVIDUAL), judged by
--    Ivan Ramil. score is the 0-10 raw value; the sheet's "Média" = score * coef
--    (that weighted total is not stored here, it feeds final_score in section 3).
--    IMPORTANT: the scoresheet lists exercises in the order they were RUN at the
--    event (the sheet's row order), which for Classe 1/2/3 differs from the
--    configuration's exercise numbering. Each score below is keyed to the exercise
--    it actually belongs to (matched by name/coef), NOT to the sheet row index.
--    See section 2 for the per-event run order (position). COBS ran in numbering
--    order, so there rows and numbering coincide.
--    Exercises not performed (blank on the sheet) are recorded with score 0. Dogs
--    with no scoresheet (avsc-dog-2, avsc-dog-12) and the disqualified avsc-dog-15
--    have no rows.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_scores (event_id, exercise_id, judge_id, dog_id, score, created_at, last_update)
VALUES -- Classe 1: avsc-dog-1 (dorsal 1, Leana Dias / Bali) — exercise 1.5 (sheet row 7) not performed, entered as 0
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.1_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.2_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.3_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.4_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.5_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.6_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.7_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.8_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770421009', 'OBDX_FCI_GRADE_1.9_V0', 'judge_1783770129340', '621XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 2: avsc-dog-3 (dorsal 3, António Dias / Simply Black Despacito)
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.1_V0', 'judge_1783770129340', '620095300051547', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.2_V0', 'judge_1783770129340', '620095300051547', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.3_V0', 'judge_1783770129340', '620095300051547', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.4_V0', 'judge_1783770129340', '620095300051547', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.5_V0', 'judge_1783770129340', '620095300051547', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.6_V0', 'judge_1783770129340', '620095300051547', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.7_V0', 'judge_1783770129340', '620095300051547', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.8_V0', 'judge_1783770129340', '620095300051547', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.9_V0', 'judge_1783770129340', '620095300051547', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.10_V0', 'judge_1783770129340', '620095300051547', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 2: avsc-dog-4 (dorsal 4, Manuel Cunha / Joy) — exercise 2.5 (sheet row 3) not performed, entered as 0
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.1_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.2_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.3_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.4_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.5_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.6_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.7_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.8_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.9_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.10_V0', 'judge_1783770129340', '622XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 2: avsc-dog-5 (dorsal 5, Ana Isabel Pereira / Zidane) — exercises 2.5 and 2.8 (sheet rows 3 and 7) not performed, entered as 0
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.1_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.2_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.3_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.4_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.5_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.6_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.7_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.8_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.9_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770430523', 'OBDX_FCI_GRADE_2.10_V0', 'judge_1783770129340', '623XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 3: avsc-dog-6 (dorsal 6, Florinda Sampaio / Thor) — exercise 3.6 (sheet row 8) not performed, entered as 0
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.1_V0', 'judge_1783770129340', '380260102376914', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.2_V0', 'judge_1783770129340', '380260102376914', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.3_V0', 'judge_1783770129340', '380260102376914', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.4_V0', 'judge_1783770129340', '380260102376914', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.5_V0', 'judge_1783770129340', '380260102376914', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.6_V0', 'judge_1783770129340', '380260102376914', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.7_V0', 'judge_1783770129340', '380260102376914', 6.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.8_V0', 'judge_1783770129340', '380260102376914', 6.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.9_V0', 'judge_1783770129340', '380260102376914', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.10_V0', 'judge_1783770129340', '380260102376914', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 3: avsc-dog-7 (dorsal 7, Sonia Grande / Hooked On Obedience) — exercises 3.7, 3.9, 3.1, 3.2 (sheet rows 4, 7, 9, 10) not performed, entered as 0
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.1_V0', 'judge_1783770129340', '945000001842619', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.2_V0', 'judge_1783770129340', '945000001842619', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.3_V0', 'judge_1783770129340', '945000001842619', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.4_V0', 'judge_1783770129340', '945000001842619', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.5_V0', 'judge_1783770129340', '945000001842619', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.6_V0', 'judge_1783770129340', '945000001842619', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.7_V0', 'judge_1783770129340', '945000001842619', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.8_V0', 'judge_1783770129340', '945000001842619', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.9_V0', 'judge_1783770129340', '945000001842619', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.10_V0', 'judge_1783770129340', '945000001842619', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 3: avsc-dog-8 (dorsal 8, Manuela Prieto / Dea Diva)
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.1_V0', 'judge_1783770129340', '978000040086211', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.2_V0', 'judge_1783770129340', '978000040086211', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.3_V0', 'judge_1783770129340', '978000040086211', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.4_V0', 'judge_1783770129340', '978000040086211', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.5_V0', 'judge_1783770129340', '978000040086211', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.6_V0', 'judge_1783770129340', '978000040086211', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.7_V0', 'judge_1783770129340', '978000040086211', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.8_V0', 'judge_1783770129340', '978000040086211', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.9_V0', 'judge_1783770129340', '978000040086211', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.10_V0', 'judge_1783770129340', '978000040086211', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- Classe 3: avsc-dog-9 (dorsal 9, António Dias / Chita) — exercises 3.7, 3.10, 3.6 (sheet rows 4, 6, 8) not performed, entered as 0
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.1_V0', 'judge_1783770129340', '620098500124755', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.2_V0', 'judge_1783770129340', '620098500124755', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.3_V0', 'judge_1783770129340', '620098500124755', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.4_V0', 'judge_1783770129340', '620098500124755', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.5_V0', 'judge_1783770129340', '620098500124755', 6.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.6_V0', 'judge_1783770129340', '620098500124755', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.7_V0', 'judge_1783770129340', '620098500124755', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.8_V0', 'judge_1783770129340', '620098500124755', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.9_V0', 'judge_1783770129340', '620098500124755', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770452508', 'OBDX_FCI_GRADE_3.10_V0', 'judge_1783770129340', '620098500124755', 0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- COBS: avsc-dog-10 (dorsal 10, Arnauld Jambart / Vince Vermute)
       ('event_1783770408793', 'OBDX_CPC_COBS.1_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.2_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.3_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.4_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.5_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.6_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.7_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.8_V0', 'judge_1783770129340', '624XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- COBS: avsc-dog-11 (dorsal 11, Gonçalo Guerreiro / Blaze)
       ('event_1783770408793', 'OBDX_CPC_COBS.1_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.2_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.3_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.4_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.5_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.6_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.7_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.8_V0', 'judge_1783770129340', '963XXXXXXXXXXXX', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- COBS: avsc-dog-13 (dorsal 13, Sally Tinkle / Valentine's Night Magic)
       ('event_1783770408793', 'OBDX_CPC_COBS.1_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.2_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.3_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 5.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.4_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.5_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.6_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.7_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.8_V0', 'judge_1783770129340', '956XXXXXXXXXXXX', 7.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       -- COBS: avsc-dog-14 (dorsal 14, Joana Santos / Dream Shannara)
       ('event_1783770408793', 'OBDX_CPC_COBS.1_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 10.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.2_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 8.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.3_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.4_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 8.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.5_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 7.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.6_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 9.5,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.7_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
       ('event_1783770408793', 'OBDX_CPC_COBS.8_V0', 'judge_1783770129340', '992XXXXXXXXXXXX', 9.0,
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000));

COMMIT;
