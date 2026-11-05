package com.abolore.ledger_pay.webhooks.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_outbox", uniqueConstraints = {
        @UniqueConstraint(name = "uq_webhook_outbox_provider_deduplication", columnNames = { "provider",
                "deduplication_key" })
}, indexes = {
        @Index(name = "idx_webhook_outbox_pending", columnList = "next_attempt_at, created_at"),
        @Index(name = "idx_webhook_outbox_expired_lease", columnList = "lease_until")
})

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "deduplication_key", nullable = false, length = 200)
    private String deduplicationKey;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookOutboxStatus status = WebhookOutboxStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "claim_token")
    private UUID claimToken;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (this.id == null) {
            this.id = UUID.randomUUID();
        }

        if (this.status == null) {
            this.status = WebhookOutboxStatus.PENDING;
        }

        if (this.nextAttemptAt == null) {
            this.nextAttemptAt = now;
        }

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public static class Builder {
        private UUID id;
        private String provider;
        private String eventType;
        private String deduplicationKey;
        private String transactionReference;
        private String payload;
        private WebhookOutboxStatus status;
        private int attemptCount;
        private Instant nextAttemptAt;
        private UUID claimToken;
        private Instant leaseUntil;
        private Instant publishedAt;
        private String lastError;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder setEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setDeduplicationKey(String deduplicationKey) {
            this.deduplicationKey = deduplicationKey;
            return this;
        }

        public Builder setTransactionReference(String transactionReference) {
            this.transactionReference = transactionReference;
            return this;
        }

        public Builder setPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder setStatus(WebhookOutboxStatus status) {
            this.status = status;
            return this;
        }

        public Builder setAttemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public Builder setNextAttemptAt(Instant nextAttemptAt) {
            this.nextAttemptAt = nextAttemptAt;
            return this;
        }

        public Builder setClaimToken(UUID claimToken) {
            this.claimToken = claimToken;
            return this;
        }

        public Builder setLeaseUntil(Instant leaseUntil) {
            this.leaseUntil = leaseUntil;
            return this;
        }

        public Builder setPublishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder setLastError(String lastError) {
            this.lastError = lastError;
            return this;
        }

        public WebhookOutbox build() {
            return new WebhookOutbox(this);
        }
    }

    private WebhookOutbox(Builder builder) {
        this.id = builder.id;
        this.provider = builder.provider;
        this.eventType = builder.eventType;
        this.deduplicationKey = builder.deduplicationKey;
        this.transactionReference = builder.transactionReference;
        this.payload = builder.payload;
        this.status = builder.status != null ? builder.status : WebhookOutboxStatus.PENDING;
        this.attemptCount = builder.attemptCount;
        this.nextAttemptAt = builder.nextAttemptAt != null ? builder.nextAttemptAt : Instant.now();
        this.claimToken = builder.claimToken;
        this.leaseUntil = builder.leaseUntil;
        this.publishedAt = builder.publishedAt;
        this.lastError = builder.lastError;
    }

    public void claim(UUID claimToken, Instant leaseUntil) {
        this.status = WebhookOutboxStatus.PROCESSING;
        this.claimToken = claimToken;
        this.leaseUntil = leaseUntil;
        this.attemptCount++;
    }

    public void markPublished(UUID claimToken) {
        verifyClaim(claimToken);

        this.status = WebhookOutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.claimToken = null;
        this.leaseUntil = null;
        this.lastError = null;
    }

    public void markFailed(UUID claimToken, String error) {
        verifyClaim(claimToken);

        this.status = WebhookOutboxStatus.PENDING;
        this.lastError = error;
        this.claimToken = null;
        this.leaseUntil = null;
    }

    private void verifyClaim(UUID claimToken) {
        if (this.claimToken == null || !this.claimToken.equals(claimToken)) {
            throw new IllegalStateException("Invalid webhook outbox claim token");
        }
    }

}
