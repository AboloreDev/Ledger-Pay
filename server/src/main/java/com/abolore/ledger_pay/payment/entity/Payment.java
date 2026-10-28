package com.abolore.ledger_pay.payment.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.payment.exceptions.InvalidPaymentException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_payments_reference", columnNames = "reference")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String reference;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider_checkout_id", unique = true, length = 150)
    private String providerCheckoutId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "provider_reference", length = 150)
    private String providerReference;

    @Column(name = "checkout_url")
    private String checkoutUrl;

    protected Payment() {
        // Required by JPA
    }

    public static class Builder {

        private String reference;
        private BigDecimal amount;
        private String currency;
        private UUID id;
        private String providerCheckoutId;
        private String providerReference;
        private String checkoutUrl;

        public Builder setReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setProviderCheckoutId(String checkoutID) {
            this.providerCheckoutId = checkoutID;
            return this;
        }

        public Builder setProviderReference(String providerReference) {
            this.providerReference = providerReference;
            return this;
        }

        public Builder setCheckoutUrl(String checkoutUrl) {
            this.checkoutUrl = checkoutUrl;
            return this;
        }

        public Payment build() {
            if (reference == null || reference.isBlank()) {
                throw new InvalidPaymentException("Payment reference cannot be empty");
            }

            if (amount == null || amount.signum() <= 0) {
                throw new InvalidPaymentException(
                        "Payment amount must be a positive value");
            }

            if (amount.scale() > 2) {
                throw new InvalidPaymentException(
                        "Payment amount cannot have more than 2 decimal places");
            }

            if (currency == null
                    || !currency.matches("[A-Z]{3}")) {
                throw new InvalidPaymentException(
                        "Currency must be a 3-letter uppercase code");
            }

            return new Payment(this);
        }

    }

    private Payment(Builder builder) {
        this.reference = builder.reference;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.status = PaymentStatus.PENDING;
        this.providerCheckoutId = builder.providerCheckoutId;
        this.providerReference = builder.providerReference;
        this.checkoutUrl = builder.checkoutUrl;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getProviderCheckoutId() {
        return providerCheckoutId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    // METHODS
    public void markSucceeded() {
        requirePending();
        this.status = PaymentStatus.SUCCEEDED;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        requirePending();
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    private void requirePending() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidPaymentException(
                    "Only pending payments can change outcome. Current status: "
                            + this.status);
        }
    }

    public void setProviderCheckoutId(String providerCheckoutId) {
        this.providerCheckoutId = providerCheckoutId;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}
