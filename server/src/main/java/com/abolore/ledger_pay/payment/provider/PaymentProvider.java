package com.abolore.ledger_pay.payment.provider;

public interface PaymentProvider {

    PaymentInitiationResult initiatePayment(PaymentInitiationRequest request);

    PaymentVerificationResult verifyPayment(String providerReference);
}