package com.abolore.ledger_pay.merchant_keys.entity;

import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.merchants.entity.Merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchant_api_keys")
public class MerchantApiKey {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, unique = true, length = 30)
    private String keyPrefix;

    @Column(nullable = false, unique = true, length = 255)
    private String keyHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantApiKeyStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant revokedAt;

    protected MerchantApiKey() {
    }

    public static class Builder {
        private UUID id;
        private Merchant merchant;
        private String keyPrefix;
        private String keyHash;
        private MerchantApiKeyStatus status;
        private Instant createdAt;
        private Instant revokedAt;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setMerchant(Merchant merchant) {
            this.merchant = merchant;
            return this;
        }

        public Builder setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        public Builder setKeyHash(String keyHash) {
            this.keyHash = keyHash;
            return this;
        }

        public Builder setStatus(MerchantApiKeyStatus status) {
            this.status = status;
            return this;
        }

        public Builder setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setRevokedAt(Instant revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public MerchantApiKey build() {
            return new MerchantApiKey(this);
        }
    }

    protected MerchantApiKey(Builder builder) {
        this.id = builder.id;
        this.merchant = builder.merchant;
        this.keyPrefix = builder.keyPrefix;
        this.keyHash = builder.keyHash;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.revokedAt = builder.revokedAt;
    }

    public static MerchantApiKey create(Merchant merchant, String keyPrefix, String keyHash) {

        return new MerchantApiKey.Builder()
                .setMerchant(merchant)
                .setKeyPrefix(keyPrefix)
                .setKeyHash(keyHash)
                .setStatus(MerchantApiKeyStatus.ACTIVE)
                .setCreatedAt(Instant.now())
                .build();
    }

    public void revoke() {
        if (this.status == MerchantApiKeyStatus.REVOKED) {
            return;
        }

        this.status = MerchantApiKeyStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public MerchantApiKeyStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

}