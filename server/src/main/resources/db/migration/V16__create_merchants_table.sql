CREATE TABLE merchants (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_merchants_email UNIQUE (email),
    CONSTRAINT chk_merchant_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);