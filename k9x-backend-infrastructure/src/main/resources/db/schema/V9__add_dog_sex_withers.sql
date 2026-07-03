-- Add physical trait fields to k9x.dogs:
--   sex        -> MALE or FEMALE, nullable for backward compatibility with existing rows
--   withers_cm -> height at the withers, in centimeters
ALTER TABLE k9x.dogs
    ADD COLUMN sex        VARCHAR(10),
    ADD COLUMN withers_cm INTEGER,
    ADD CONSTRAINT k9x_dogs_sex_check
        CHECK (sex IS NULL OR sex IN ('MALE', 'FEMALE'));
