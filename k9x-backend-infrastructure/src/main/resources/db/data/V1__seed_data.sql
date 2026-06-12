-- =====================================================================
-- Seed data (loaded by the dataFlyway instance into db/data)
-- =====================================================================

-- users -----------------------------------------------------------------
INSERT INTO k9x.users (id, email, image) VALUES
    ('user-1', 'txomin.sirera@gmail.com', '');

-- organizers ------------------------------------------------------------
INSERT INTO k9x.organizers (user_id, name) VALUES
    ('user-1', 'Brincan');

-- judges ----------------------------------------------------------------
INSERT INTO k9x.judges (id, name, creator, last_update, created_at, deleted_at) VALUES
    ('judge-1', 'Default Judge', 'txomin.sirera@gmail.com', 1700000000000, 1700000000000, NULL);

-- dogs ------------------------------------------------------------------
INSERT INTO k9x.dogs (id, identity, breed, name, image, owner, creator, country, team, last_update, created_at, deleted_at) VALUES
    ('dog-1', 'ES-2021-001', 'Border Collie', 'Rex',  '', 'txomin.sirera@gmail.com', 'txomin.sirera@gmail.com', 'ES', 'Team A', 1700000000000, 1700000000000, NULL),
    ('dog-2', 'ES-2022-014', 'Malinois',      'Luna', '', 'txomin.sirera@gmail.com', 'txomin.sirera@gmail.com', 'ES', 'Team A', 1700000000000, 1700000000000, NULL);

-- competitions ----------------------------------------------------------
INSERT INTO k9x.competitions (id, name, country, description, address, coord_alt, coord_long, creator, last_update, created_at, deleted_at) VALUES
    ('comp-1', 'Copa Brincan 2026', 'ES', 'Competición de obediencia OBDX', 'Madrid, Spain', 40.4168, -3.7038, 'txomin.sirera@gmail.com', 1700000000000, 1700000000000, NULL);

-- stages ----------------------------------------------------------------
INSERT INTO k9x.stages (id, name, competition_id, date_from, date_to, creator, last_update, created_at, deleted_at) VALUES
    ('stage-1', 'Jornada 1', 'comp-1', 1782864000000, 1782950400000, 'txomin.sirera@gmail.com', 1700000000000, 1700000000000, NULL),
    ('stage-2', 'Jornada 2', 'comp-1', 1784073600000, 1784160000000, 'txomin.sirera@gmail.com', 1700000000000, 1700000000000, NULL);

-- events ----------------------------------------------------------------
-- stage-1: 1 event (RSCE debutante)
-- stage-2: COBS + FCI grades 1, 2, 3
INSERT INTO k9x.events (id, discipline, configuration_id, score_calculation, name, creator, stage_id, enrollment_deadline, last_update, created_at, deleted_at) VALUES
    ('event-1', 'OBDX', 'OBDX_RSCE_DEBUTANTE_V0', 'AVG', 'RSCE Debutante', 'txomin.sirera@gmail.com', 'stage-1', NULL, 1700000000000, 1700000000000, NULL),
    ('event-2', 'OBDX', 'CPC_COBS_V0',            'AVG', 'COBS',           'txomin.sirera@gmail.com', 'stage-2', NULL, 1700000000000, 1700000000000, NULL),
    ('event-3', 'OBDX', 'OBDX_FCI_GRADE_1_V0',    'AVG', 'FCI Grado 1',    'txomin.sirera@gmail.com', 'stage-2', NULL, 1700000000000, 1700000000000, NULL),
    ('event-4', 'OBDX', 'OBDX_FCI_GRADE_2_V0',    'AVG', 'FCI Grado 2',    'txomin.sirera@gmail.com', 'stage-2', NULL, 1700000000000, 1700000000000, NULL),
    ('event-5', 'OBDX', 'OBDX_FCI_GRADE_3.V0',    'AVG', 'FCI Grado 3',    'txomin.sirera@gmail.com', 'stage-2', NULL, 1700000000000, 1700000000000, NULL);

-- event_competitors (both dogs in every event) --------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, position, verified, last_update, not_competing) VALUES
    ('event-1', 'dog-1', NULL, NULL, 1700000000000, FALSE),
    ('event-1', 'dog-2', NULL, NULL, 1700000000000, FALSE),
    ('event-2', 'dog-1', NULL, NULL, 1700000000000, FALSE),
    ('event-2', 'dog-2', NULL, NULL, 1700000000000, FALSE),
    ('event-3', 'dog-1', NULL, NULL, 1700000000000, FALSE),
    ('event-3', 'dog-2', NULL, NULL, 1700000000000, FALSE),
    ('event-4', 'dog-1', NULL, NULL, 1700000000000, FALSE),
    ('event-4', 'dog-2', NULL, NULL, 1700000000000, FALSE),
    ('event-5', 'dog-1', NULL, NULL, 1700000000000, FALSE),
    ('event-5', 'dog-2', NULL, NULL, 1700000000000, FALSE);

-- event_judges (judge-1 with user-1 as collector in every event) --------
INSERT INTO obdx.event_judges (event_id, judge_id, collector_id, ring, last_update) VALUES
    ('event-1', 'judge-1', 'user-1', 1, 1700000000000),
    ('event-2', 'judge-1', 'user-1', 2, 1700000000000),
    ('event-3', 'judge-1', 'user-1', 3, 1700000000000),
    ('event-4', 'judge-1', 'user-1', 4, 1700000000000),
    ('event-5', 'judge-1', 'user-1', 5, 1700000000000);

-- event_exercises (first 4 exercises of each event's configuration) ------
INSERT INTO obdx.event_exercises (event_id, exercise_id, position, tags, last_update) VALUES
    ('event-1', 'OBDX_RSCE_DEBUTANTE.1_V0', 1, NULL, 1700000000000),
    ('event-1', 'OBDX_RSCE_DEBUTANTE.2_V0', 2, NULL, 1700000000000),
    ('event-1', 'OBDX_RSCE_DEBUTANTE.3_V0', 3, NULL, 1700000000000),
    ('event-1', 'OBDX_RSCE_DEBUTANTE.4_V0', 4, NULL, 1700000000000),
    ('event-2', 'OBDX_CPC_COBS.1_V0', 1, NULL, 1700000000000),
    ('event-2', 'OBDX_CPC_COBS.2_V0', 2, NULL, 1700000000000),
    ('event-2', 'OBDX_CPC_COBS.3_V0', 3, NULL, 1700000000000),
    ('event-2', 'OBDX_CPC_COBS.4_V0', 4, NULL, 1700000000000),
    ('event-3', 'OBDX_FCI_GRADE_1.1_V0', 1, NULL, 1700000000000),
    ('event-3', 'OBDX_FCI_GRADE_1.2_V0', 2, NULL, 1700000000000),
    ('event-3', 'OBDX_FCI_GRADE_1.3_V0', 3, NULL, 1700000000000),
    ('event-3', 'OBDX_FCI_GRADE_1.4_V0', 4, NULL, 1700000000000),
    ('event-4', 'OBDX_FCI_GRADE_2.1_V0', 1, NULL, 1700000000000),
    ('event-4', 'OBDX_FCI_GRADE_2.2_V0', 2, NULL, 1700000000000),
    ('event-4', 'OBDX_FCI_GRADE_2.3_V0', 3, NULL, 1700000000000),
    ('event-4', 'OBDX_FCI_GRADE_2.4_V0', 4, NULL, 1700000000000),
    ('event-5', 'OBDX_FCI_GRADE_3.1_V0', 1, NULL, 1700000000000),
    ('event-5', 'OBDX_FCI_GRADE_3.2_V0', 2, NULL, 1700000000000),
    ('event-5', 'OBDX_FCI_GRADE_3.3_V0', 3, NULL, 1700000000000),
    ('event-5', 'OBDX_FCI_GRADE_3.4_V0', 4, NULL, 1700000000000);
