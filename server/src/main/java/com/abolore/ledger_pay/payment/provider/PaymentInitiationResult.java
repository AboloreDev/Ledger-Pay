package com.abolore.ledger_pay.payment.provider;

public record PaymentInitiationResult(String providerReference, String checkoutUrl) {
}