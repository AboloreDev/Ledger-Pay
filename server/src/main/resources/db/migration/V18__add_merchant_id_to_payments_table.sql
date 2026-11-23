ALTER TABLE payments
    ADD COLUMN merchant_id UUID;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_merchant
    FOREIGN KEY (merchant_id)
    REFERENCES merchants(id);

CREATE INDEX idx_payments_merchant_id
    ON payments(merchant_id);