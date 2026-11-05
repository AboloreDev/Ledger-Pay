
CREATE TABLE webhook_outbox (
    id UUID PRIMARY KEY,

    provider VARCHAR(30) NOT NULL,
    event_type VARCHAR(100) NOT NULL,

    deduplication_key VARCHAR(200) NOT NULL,
    transaction_reference VARCHAR(100),

    payload JSONB NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    claim_token UUID,
    lease_until TIMESTAMPTZ,

    published_at TIMESTAMPTZ,
    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_webhook_outbox_provider_deduplication
        UNIQUE (provider, deduplication_key),

    CONSTRAINT chk_webhook_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')),

    CONSTRAINT chk_webhook_outbox_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_webhook_outbox_pending
    ON webhook_outbox (next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_webhook_outbox_expired_lease
    ON webhook_outbox (lease_until)
    WHERE status = 'PROCESSING';
