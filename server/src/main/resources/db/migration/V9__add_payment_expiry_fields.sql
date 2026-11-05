ALTER TABLE payments
    ADD COLUMN expires_at TIMESTAMPTZ,
    ADD COLUMN paid_at TIMESTAMPTZ,
    ADD COLUMN late_payment BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_payments_status_expires_at
    ON payments (status, expires_at);