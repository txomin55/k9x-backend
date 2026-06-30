-- Split the dog ownership concepts:
--   owner   -> the app user (email) that owns the dog; basis for authorization
--   handler -> free-text label for the person handling the dog (e.g. 'Smoke Owner')
-- Existing rows had a free-text name stored in `owner`; move it to `handler` and
-- re-point `owner` to the row creator so authorization works again.
ALTER TABLE k9x.dogs
    ADD COLUMN handler VARCHAR(255);

UPDATE k9x.dogs
SET handler = owner,
    owner   = creator
WHERE owner IS DISTINCT FROM creator;
