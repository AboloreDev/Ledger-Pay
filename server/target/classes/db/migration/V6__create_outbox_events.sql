
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,

    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN (
            'PENDING',
            'PROCESSING',
            'PROCESSED',
            'FAILED'
        )),

    attempts INTEGER NOT NULL DEFAULT 0
        CHECK (attempts >= 0),

    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    locked_until TIMESTAMPTZ,
    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,

    CONSTRAINT chk_outbox_processing_lock
    CHECK (
        (status = 'PROCESSING' AND locked_until IS NOT NULL)
        OR
        (status <> 'PROCESSING' AND locked_until IS NULL)
    )
);

CREATE INDEX idx_outbox_pending_events
    ON outbox_events (available_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_expired_locks
    ON outbox_events (locked_until)
    WHERE status = 'PROCESSING';
