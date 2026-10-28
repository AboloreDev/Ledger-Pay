ALTER TABLE payments
    ADD COLUMN provider_reference VARCHAR(150),
    ADD COLUMN checkout_url TEXT;

CREATE UNIQUE INDEX uq_payments_provider_reference
    ON payments (provider_reference)
    WHERE provider_reference IS NOT NULL;