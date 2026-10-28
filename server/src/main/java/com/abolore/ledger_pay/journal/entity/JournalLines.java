package com.abolore.ledger_pay.journal.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.ledger.entity.LedgerAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "journal_lines")
public class JournalLines {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntries journalEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ledger_account_id", nullable = false)
    private LedgerAccount ledgerAccount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JournalSides side;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static class Builder {
        private JournalEntries journalEntry;
        private LedgerAccount ledgerAccount;
        private String currency;
        private JournalSides side;
        private BigDecimal amount;

        public Builder() {
        }

        public Builder setJournalEntry(JournalEntries journalEntry) {
            this.journalEntry = journalEntry;
            return this;
        }

        public Builder setLedgerAccount(LedgerAccount ledgerAccount) {
            this.ledgerAccount = ledgerAccount;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setSide(JournalSides side) {
            this.side = side;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public JournalLines build() {
            return new JournalLines(this);
        }
    }

    private JournalLines(Builder builder) {
        this.journalEntry = builder.journalEntry;
        this.ledgerAccount = builder.ledgerAccount;
        this.currency = builder.currency;
        this.side = builder.side;
        this.amount = builder.amount;
        this.createdAt = Instant.now();
    }

    protected JournalLines() {
    }

    public UUID getId() {
        return id;
    }

    public JournalEntries getJournalEntry() {
        return journalEntry;
    }

    public LedgerAccount getLedgerAccount() {
        return ledgerAccount;
    }

    public String getCurrency() {
        return currency;
    }

    public JournalSides getSide() {
        return side;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
