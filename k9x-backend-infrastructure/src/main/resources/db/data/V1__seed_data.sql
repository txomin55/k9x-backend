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
-- Synthetic judge that owns the score rows of imported events: an import carries a final total, not the
-- per-judge marks that produced it, but obdx.event_scores.judge_id is part of the primary key and points at
-- k9x.judges. It is never shown: the classification ignores the judge of a OBDX.FINAL_SCORE row.
INSERT INTO k9x.judges (id, name, creator, last_update, created_at, country)
VALUES ('UNKNOWN', 'Unknown', 'k9x.support@gmail.com', 0, 0, '');
