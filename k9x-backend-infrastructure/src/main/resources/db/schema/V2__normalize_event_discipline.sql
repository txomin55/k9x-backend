-- Normalize the discipline identifier to its canonical uppercase form (e.g. 'obdx' -> 'OBDX').
-- Writes are normalized at the domain level (CompetitionAggregate#createEvent); this fixes pre-existing rows.
UPDATE k9x.events
SET discipline = UPPER(discipline)
WHERE discipline IS NOT NULL
  AND discipline <> UPPER(discipline);
