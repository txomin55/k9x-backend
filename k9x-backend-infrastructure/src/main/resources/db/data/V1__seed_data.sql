-- =====================================================================
-- Seed data (loaded by the dataFlyway instance into db/data)
-- =====================================================================

-- users -----------------------------------------------------------------
INSERT INTO k9x.users (id, email, image)
VALUES ('k9x.support@gmail.com', 'k9x.support@gmail.com',
        'https://lh3.googleusercontent.com/a/ACg8ocIV5lb10eN1IfN2V1sipto5wWssNjDiBnUHiUdLTg7ubqbrrQ=s96-c');

-- organizers ------------------------------------------------------------
INSERT INTO k9x.organizers (user_id, name)
VALUES ('k9x.support@gmail.com', 'Nos');
