
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    reference VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_payments_reference
        UNIQUE (reference),

    CONSTRAINT chk_payments_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_payments_currency_format
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_payments_status
        CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED'))
);
