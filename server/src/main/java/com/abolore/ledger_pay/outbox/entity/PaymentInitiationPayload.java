package com.abolore.ledger_pay.outbox.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiationPayload(
                UUID paymentId,
                String reference,
                BigDecimal amount,
                String currency,
                String customerEmail) {
}