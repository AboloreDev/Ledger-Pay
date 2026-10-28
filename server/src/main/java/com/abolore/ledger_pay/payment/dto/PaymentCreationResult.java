package com.abolore.ledger_pay.payment.dto;

import com.abolore.ledger_pay.payment.entity.Payment;

public record PaymentCreationResult(
        Payment payment,
        String checkoutUrl) {
}