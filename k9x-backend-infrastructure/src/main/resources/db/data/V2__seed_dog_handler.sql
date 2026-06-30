-- Backfill the `handler` (free-text label) for the seed dogs.
-- Added as a new data migration so V1__seed_data.sql keeps its original checksum
-- on databases that already applied it.
UPDATE k9x.dogs SET handler = 'Rex Handler'  WHERE id = 'dog-1';
UPDATE k9x.dogs SET handler = 'Luna Handler' WHERE id = 'dog-2';
