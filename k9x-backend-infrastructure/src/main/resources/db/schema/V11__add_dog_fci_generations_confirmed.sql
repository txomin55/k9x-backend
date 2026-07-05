-- Add 3fciGenerationsConfirmed field to k9x.dogs: whether 3 FCI generations have been confirmed for the dog.
-- Leading underscore because SQL identifiers cannot start with a digit.
ALTER TABLE k9x.dogs
    ADD COLUMN _3fci_generations_confirmed BOOLEAN;
