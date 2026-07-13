-- =============================================================================
-- 20º Trofeu AVSC: k9x schema data (judge, competition, stage, events, dogs),
-- sourced from competition 'competition_1783770154788' and proba.jpeg.
--
-- This is a one-off data script, NOT a Flyway migration/seed: run it manually
-- against a target database after the schema/seed migrations have been applied.
-- Run this file BEFORE trofeu_avsc_obdx.sql, which references these rows via FK.
--
-- Load order: judge -> competition -> stage -> events -> dogs, respecting FK
-- dependencies. IDs and timestamps preserve the original data.
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 0) Organizer user (A.V. Super Cães) — also the event collector (see obdx).
--    image is NOT NULL with no source value, left as ''.
-- ---------------------------------------------------------------------------
INSERT INTO k9x.users (id, email, image)
VALUES ('acsupercaes@gmail.com', 'acsupercaes@gmail.com', '');

INSERT INTO k9x.organizers (user_id, name)
VALUES ('acsupercaes@gmail.com', 'A.V. Super Cães');

-- ---------------------------------------------------------------------------
-- 1) Judge (Ivan Ramil)
-- ---------------------------------------------------------------------------
INSERT INTO k9x.judges (id, name, creator, last_update, created_at, deleted_at, country)
VALUES ('judge_1783770129340', 'Ivan Ramil', 'k9x.support@gmail.com', 1783770147887,
        1783770147887, NULL, 'ES');

-- ---------------------------------------------------------------------------
-- 2) Competition
-- ---------------------------------------------------------------------------
INSERT INTO k9x.competitions (id, name, country, description, address, coord_alt, coord_long, creator,
                              last_update, created_at, deleted_at)
VALUES ('competition_1783770154788', '20º Trofeu AVSC', 'PT', '20º Trofeu AVSC proba nocturna',
        'R. da Liberdade, 4810-689 Abação (São Tomé), Portugal', 41.4002853, -8.2702335, 'k9x.support@gmail.com',
        1783770385334, 1783770154823, NULL);

-- ---------------------------------------------------------------------------
-- 3) Stage
-- ---------------------------------------------------------------------------
INSERT INTO k9x.stages (id, name, competition_id, date_from, date_to, creator, last_update, created_at,
                        deleted_at)
VALUES ('stage_1783770388394', 'Proba Nocturna', 'competition_1783770154788', 1783728000000, 1783814399999,
        'k9x.support@gmail.com', 1783770397902, 1783770397902, NULL);

-- ---------------------------------------------------------------------------
-- 4) Events (discipline OBDX = obedience)
-- ---------------------------------------------------------------------------
INSERT INTO k9x.events (id, discipline, configuration_id, score_calculation, name, creator, stage_id,
                        enrollment_deadline, last_update, created_at, deleted_at, awards, rank)
VALUES ('event_1783770421009', 'OBDX', 'OBDX_FCI_GRADE_1_V0', 'AVG', 'Classe 1', 'k9x.support@gmail.com',
        'stage_1783770388394', 1783295999999, 1783770635598, 1783770428198, NULL, ARRAY []::VARCHAR[], NULL),
       ('event_1783770430523', 'OBDX', 'OBDX_FCI_GRADE_2_V0', 'AVG', 'Classe 2', 'k9x.support@gmail.com',
        'stage_1783770388394', 1783295999999, 1783770623129, 1783770439363, NULL, ARRAY []::VARCHAR[], NULL),
       ('event_1783770452508', 'OBDX', 'OBDX_FCI_GRADE_3.V0', 'AVG', 'Classe 3', 'k9x.support@gmail.com',
        'stage_1783770388394', 1783295999999, 1783770606029, 1783770460401, NULL, ARRAY []::VARCHAR[], NULL),
       ('event_1783770408793', 'OBDX', 'CPC_COBS_V0', 'AVG', 'COBS', 'k9x.support@gmail.com',
        'stage_1783770388394', 1783295999999, 1783770506599, 1783770415880, NULL, ARRAY []::VARCHAR[], NULL);

-- ---------------------------------------------------------------------------
-- 5) Dogs (from proba.jpeg, one row per competitor: handler = Nome Condutor,
--    name = Nome Cão, team = Clube, sex from Género). Breeds mapped to the Breed
--    enum (SRD has no enum value -> 'UNKNOWN'). Names/clubs normalised to Title
--    Case (A.V. initialism preserved). All competitors are Portuguese ('PT')
--    except Sonia Grande and Manuela Prieto ('ES'). identity not in source -> ''.
-- ---------------------------------------------------------------------------
INSERT INTO k9x.dogs (id, identity, breed, name, image, owner, creator, country, team, handler,
                      last_update, created_at, deleted_at, three_fci_generations_confirmed, sex)
VALUES  -- Classe 1
       ('621XXXXXXXXXXXX', 'OP638636', 'WHITE_SWISS_SHEPHERD_DOG', 'Bali From House Black&White', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Gugadogs Centro De Treino & Educação Canina', 'Leana Dias',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
       ('avsc-dog-2', '', 'BORDER_COLLIE', 'Angel Do Sonho Do Cão', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Afeto', 'Eva Flor Lobo',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
        -- Classe 2
       ('620095300051547', 'OP604806', 'RIESENSCHNAUZER', 'Simply Black Despacito', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Gugadogs Centro De Treino & Educação Canina', 'António Dias',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
       ('622XXXXXXXXXXXX', 'RD00695', 'UNKNOWN', 'Joy', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Afeto', 'Manuel Cunha',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
       ('623XXXXXXXXXXXX', 'OP621309', 'GERMAN_SHEPHERD', 'Zidane Vom', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Alfa Dog', 'Ana Mota Pereira',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
        -- Classe 3
       ('380260102376914', 'OP627563', 'BORDER_COLLIE', 'Mind The Dog Thor', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'A.V.Super Cães', 'Florinda Sampaio',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
       ('945000001842619', 'E2515168', 'BORDER_COLLIE', 'Hooked On Obedience De Xonnydeby', NULL, NULL,
        'k9x.support@gmail.com', 'ES', 'Obelution', 'Sonia Grande',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
       ('978000040086211', 'E2626831', 'LABRADOR_RETRIEVER', 'Dea Diva Vom Trogenbach', NULL, NULL,
        'k9x.support@gmail.com', 'ES', 'Justgolden', 'Manuela Prieto Gómez',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
       ('620098500124755', '582019', 'WHITE_SWISS_SHEPHERD_DOG', 'Chita', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Gugadogs Centro De Treino E Educação Canina', 'António Dias',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
        -- COBS
       ('624XXXXXXXXXXXX', 'LOP64811', 'DOBERMANN', 'Vince Vermute D''Ikòskylo', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'A.V.Super Cães', 'Arnauld Jambart',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
       ('963XXXXXXXXXXXX', 'OP622166', 'MALINOIS', 'BM2- Blaze Du Shroom', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Herdade Bonanza', 'Gonçalo Guerreiro',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
       ('avsc-dog-12', '', 'POODLE', 'Spirit L´Huere Blueu', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'A.V. Super Cães', 'Sally Tinkle',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
       ('956XXXXXXXXXXXX', 'R28370503', 'POODLE', 'Valentine´s Night Magic', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'A.V. Super Cães', 'Sally Tinkle',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE'),
       ('992XXXXXXXXXXXX', 'OP656174', 'POODLE', 'Dream Shannara Heart Of Steel', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Individual', 'Joana Santos',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'MALE'),
       ('625XXXXXXXXXXXX', 'OP645290', 'CANE_CORSO', 'Flor De Fidelium Cohors', NULL, NULL,
        'k9x.support@gmail.com', 'PT', 'Individual', 'Paulo Matos',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, NULL, 'FEMALE');

COMMIT;
