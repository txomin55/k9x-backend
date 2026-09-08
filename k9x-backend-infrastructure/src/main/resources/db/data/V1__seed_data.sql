-- =====================================================================
-- Seed data (loaded by the dataFlyway instance into db/data)
-- =====================================================================

-- users -----------------------------------------------------------------
INSERT INTO k9x.users (id, email, image)
VALUES ('k9x.support@gmail.com', 'k9x.support@gmail.com',
        'https://lh3.googleusercontent.com/a/ACg8ocIV5lb10eN1IfN2V1sipto5wWssNjDiBnUHiUdLTg7ubqbrrQ=s96-c');

-- organizers ------------------------------------------------------------
INSERT INTO k9x.organizers (user_id, name)
VALUES ('k9x.support@gmail.com', 'K9X Support');

-- user_subscriptions ----------------------------------------------------
INSERT INTO k9x.user_subscriptions (user_id, event_ids)
VALUES ('k9x.support@gmail.com', ARRAY[]::VARCHAR(255)[]);

-- judges ----------------------------------------------------------------
-- No synthetic judge is seeded. The anonymous judge slots that own the score rows of an event whose
-- source does not name its judges are ALWAYS numbered -- UNKNOWN_1, UNKNOWN_2... -- and each import
-- creates the ones it needs, so the seed does not have to guess how many there are. The unnumbered
-- 'UNKNOWN' used to be seeded here and coexisted with the numbered ones, which meant the same event
-- could show one slot too many.
