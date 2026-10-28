
CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY,
    account_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_ledger_account_type
        CHECK (
            account_type IN (
                'ASSET',
                'LIABILITY',
                'EQUITY',
                'REVENUE',
                'EXPENSE'
            )
        ),

    CONSTRAINT chk_ledger_account_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    CONSTRAINT uq_ledger_account_id_currency
        UNIQUE (id, currency)
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    source_type VARCHAR(40) NOT NULL,
    source_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_journal_source
        UNIQUE (source_type, source_id)
);

CREATE TABLE journal_lines (
    id UUID PRIMARY KEY,
    journal_entry_id UUID NOT NULL,
    ledger_account_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    side VARCHAR(10) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_journal_line_entry
        FOREIGN KEY (journal_entry_id)
        REFERENCES journal_entries(id),

    CONSTRAINT fk_journal_line_account_currency
        FOREIGN KEY (ledger_account_id, currency)
        REFERENCES ledger_accounts(id, currency),

    CONSTRAINT chk_journal_line_side
        CHECK (side IN ('DEBIT', 'CREDIT')),

    CONSTRAINT chk_journal_line_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_journal_lines_entry
    ON journal_lines(journal_entry_id);

CREATE INDEX idx_journal_lines_account
    ON journal_lines(ledger_account_id);
