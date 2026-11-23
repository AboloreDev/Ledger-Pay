CREATE TABLE merchant_api_keys (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    key_prefix VARCHAR(30) NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_merchant_api_keys_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id),

    CONSTRAINT uq_merchant_api_key_hash
        UNIQUE (key_hash),

    CONSTRAINT uq_merchant_api_key_prefix
        UNIQUE (key_prefix),

    CONSTRAINT chk_merchant_api_key_status
        CHECK (status IN ('ACTIVE', 'REVOKED'))
);

CREATE INDEX idx_merchant_api_keys_merchant_id
    ON merchant_api_keys(merchant_id);