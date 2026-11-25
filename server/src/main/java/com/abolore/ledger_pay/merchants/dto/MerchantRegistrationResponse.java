package com.abolore.ledger_pay.merchants.dto;

import java.time.Instant;
import java.util.UUID;

public record MerchantRegistrationResponse(
        UUID merchantId,
        String name,
        String email,
        String status,
        String apiKey,
        Instant createdAt) {
}