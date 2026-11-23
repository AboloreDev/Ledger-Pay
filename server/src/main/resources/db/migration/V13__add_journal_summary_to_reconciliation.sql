ALTER TABLE reconciliation_runs
    ADD COLUMN journals_checked INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN valid_journals INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN invalid_journals INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN failure_reason TEXT,
    ADD COLUMN trigger_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN initiated_by VARCHAR(255),
    ADD CONSTRAINT chk_reconciliation_trigger_type
        CHECK (trigger_type IN ('MANUAL', 'SCHEDULED'));
