-- Backfill `sex` and `withers_cm` for the seed dogs.
-- Added as a new data migration so V1__seed_data.sql / V2__seed_dog_handler.sql keep
-- their original checksums on databases that already applied them.
UPDATE k9x.dogs SET sex = 'MALE',   withers_cm = 52 WHERE id = 'dog-1';
UPDATE k9x.dogs SET sex = 'FEMALE', withers_cm = 58 WHERE id = 'dog-2';