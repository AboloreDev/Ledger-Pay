
-- Add a unique identifier for each accounting source event.
ALTER TABLE journal_entries
    ADD COLUMN source_event_id UUID;

-- Preserve existing development data by assigning each
-- existing journal entry its own event identifier.
UPDATE journal_entries
SET source_event_id = id
WHERE source_event_id IS NULL;

-- Every journal entry must have an event identifier.
ALTER TABLE journal_entries
    ALTER COLUMN source_event_id SET NOT NULL;

-- Allow multiple accounting events to reference the same
-- original business object.
ALTER TABLE journal_entries
    DROP CONSTRAINT uq_journal_source;

-- Prevent the same identified event from being posted
-- more than once for a given source type.
ALTER TABLE journal_entries
    ADD CONSTRAINT uq_journal_source_event
    UNIQUE (source_type, source_event_id);
