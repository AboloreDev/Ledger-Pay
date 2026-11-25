CREATE INDEX idx_journal_entry_to_merchant_payment
    ON journal_entries(source_type, source_id);
