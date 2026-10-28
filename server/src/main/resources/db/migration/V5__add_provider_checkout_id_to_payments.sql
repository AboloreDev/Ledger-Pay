ALTER TABLE payments
    ADD COLUMN provider_checkout_id VARCHAR(150);

CREATE UNIQUE INDEX uq_payments_provider_checkout_id
    ON payments (provider_checkout_id)
    WHERE provider_checkout_id IS NOT NULL;