package com.abolore.ledger_pay.merchants.dto;

import java.time.Instant;
import java.util.UUID;

import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

public record MerchantProfileResponse(
                UUID id, String name, String email, MerchantStatus status, Instant createdAt) {

}
