-- Add country to k9x.judges, mirroring k9x.dogs.country.
-- DEFAULT '' backfills existing rows so the column can be NOT NULL like the dog one.
ALTER TABLE k9x.judges
    ADD COLUMN country VARCHAR(50) NOT NULL DEFAULT '';
