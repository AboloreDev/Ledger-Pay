package com.abolore.ledger_pay.payment.provider;

import java.time.Instant;

public record PaymentVerificationResult(
                String providerReference,
                String status,
                long amountInMinorUnits,
                String currency,
                Instant paidAt) {
}