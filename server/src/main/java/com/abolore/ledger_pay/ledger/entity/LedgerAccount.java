package com.abolore.ledger_pay.ledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ledger_account_id_currency", columnNames = { "id", "currency" })
})
public class LedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_code", nullable = false, unique = true, length = 64)
    private String accountCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private LedgerAccountType accountType;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerAccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerAccount() {
        // Required by JPA.
    }

    public static class Builder {
        private UUID id;
        private String accountCode;
        private String name;
        private LedgerAccountType accountType;
        private String currency;
        private LedgerAccountStatus status = LedgerAccountStatus.ACTIVE;

        public Builder() {
        }

        public Builder setAccountCode(String accountCode) {
            this.accountCode = accountCode;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setAccountType(LedgerAccountType accountType) {
            this.accountType = accountType;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setStatus(LedgerAccountStatus status) {
            this.status = status;
            return this;
        }

        public LedgerAccount build() {
            if (accountCode == null || accountCode.isBlank()) {
                throw new IllegalStateException("Account code is required");
            }

            if (name == null || name.isBlank()) {
                throw new IllegalStateException("Account name is required");
            }

            if (accountType == null || currency == null || currency.isBlank()) {
                throw new IllegalStateException(
                        "Account type and currency are required");
            }
            return new LedgerAccount(this);
        }

    }

    private LedgerAccount(Builder builder) {
        this.accountCode = builder.accountCode;
        this.name = builder.name;
        this.accountType = builder.accountType;
        this.currency = builder.currency;
        this.status = builder.status;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getName() {
        return name;
    }

    public LedgerAccountType getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public LedgerAccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
