package com.abolore.ledger_pay.payment.provider;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiationRequest(
        UUID paymentId,
        String reference,
        BigDecimal amount,
        String currency,
        String customerEmail) {
}
