CREATE TABLE webhook_event_audit (
    id UUID PRIMARY KEY,

    provider VARCHAR(30) NOT NULL,

    deduplication_key VARCHAR(255) NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    transaction_reference VARCHAR(255),

    payload JSONB NOT NULL,

    processing_status VARCHAR(30) NOT NULL,

    attempts INTEGER NOT NULL DEFAULT 0,

    last_error TEXT,

    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    processed_at TIMESTAMPTZ,

    CONSTRAINT uq_webhook_audit_provider_event
        UNIQUE (provider, deduplication_key)
);

CREATE INDEX idx_webhook_audit_status
    ON webhook_event_audit(processing_status);

CREATE INDEX idx_webhook_audit_reference
    ON webhook_event_audit(transaction_reference);