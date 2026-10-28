package com.abolore.ledger_pay.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;

public record PaymentResponse(
        UUID id,
        String reference,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant createdAt,
        Instant updatedAt,
        String checkoutUrl) {

    public static PaymentResponse from(Payment payment) {
        return from(payment, null);
    }

    public static PaymentResponse from(
            Payment payment,
            String checkoutUrl) {

        return new PaymentResponse(
                payment.getId(),
                payment.getReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                checkoutUrl);
    }
}