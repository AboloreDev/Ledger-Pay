package com.abolore.ledger_pay.payment.provider;

public record PaymentVerificationResult(
        String providerReference,
        String status,
        long amountInMinorUnits,
        String currency) {
}