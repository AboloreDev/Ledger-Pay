package com.abolore.ledger_pay.merchants.dto;

import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

public record MerchantPrincipal(UUID merchantId, String email, String name, MerchantStatus status, Instant createdAt) {

}
