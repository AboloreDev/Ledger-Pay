
CREATE TABLE bachs_webhook_events (
    id UUID PRIMARY KEY,
    provider_event_id VARCHAR(150) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',

    CONSTRAINT uq_bachs_webhook_provider_event
        UNIQUE (provider_event_id),

    CONSTRAINT chk_bachs_webhook_event_status
        CHECK (status IN ('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED'))
);
