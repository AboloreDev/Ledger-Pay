CREATE TABLE reconciliation_results (
    id UUID PRIMARY KEY,

    reconciliation_run_id UUID NOT NULL,

    record_type VARCHAR(30) NOT NULL,

    reference VARCHAR(100),

    internal_payment_id UUID,

    matched BOOLEAN NOT NULL,

    discrepancies JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reconciliation_result_run
        FOREIGN KEY (reconciliation_run_id)
        REFERENCES reconciliation_runs(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_reconciliation_result_type
        CHECK (record_type IN (
            'PAYSTACK_TRANSACTION',
            'INTERNAL_PAYMENT',
            'JOURNAL'
        ))
);

CREATE INDEX idx_reconciliation_results_run
    ON reconciliation_results (reconciliation_run_id);

CREATE INDEX idx_reconciliation_results_payment
    ON reconciliation_results (internal_payment_id);

CREATE INDEX idx_reconciliation_results_reference
    ON reconciliation_results (reference);