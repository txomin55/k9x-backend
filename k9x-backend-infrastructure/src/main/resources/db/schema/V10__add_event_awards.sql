-- Add awards field to k9x.events: list of award ids assigned to the event
ALTER TABLE k9x.events
    ADD COLUMN awards VARCHAR(50)[];
