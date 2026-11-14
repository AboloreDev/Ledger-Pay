CREATE TABLE reconciliation_runs (
    id UUID PRIMARY KEY,

    provider VARCHAR(30) NOT NULL,

    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,

    status VARCHAR(20) NOT NULL,

    total_provider_transactions INTEGER NOT NULL DEFAULT 0,
    total_internal_payments INTEGER NOT NULL DEFAULT 0,
    matched_count INTEGER NOT NULL DEFAULT 0,
    discrepancy_count INTEGER NOT NULL DEFAULT 0,

    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_reconciliation_period
        CHECK (period_start < period_end),

    CONSTRAINT chk_reconciliation_status
        CHECK (status IN (
            'PENDING',
            'RUNNING',
            'COMPLETED',
            'PARTIAL',
            'FAILED'
        )),

    CONSTRAINT chk_reconciliation_counts
        CHECK (
            total_provider_transactions >= 0
            AND total_internal_payments >= 0
            AND matched_count >= 0
            AND discrepancy_count >= 0
        )
);

CREATE INDEX idx_reconciliation_runs_provider_period
    ON reconciliation_runs (provider, period_start, period_end);

CREATE INDEX idx_reconciliation_runs_status
    ON reconciliation_runs (status);