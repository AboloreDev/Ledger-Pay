package com.abolore.ledger_pay.merchant.entity;

import org.junit.jupiter.api.Test;

import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

class MerchantTest {

    @Test
    void shouldCreateMerchant() {
        Merchant merchant = Merchant.create("Test Merchant", "merchant@example.com");

        assertThat(merchant.getName()).isEqualTo("Test Merchant");
        assertThat(merchant.getEmail()).isEqualTo("merchant@example.com");
        assertThat(merchant.getStatus()).isEqualTo(MerchantStatus.ACTIVE);
    }

    @Test
    void shouldInitializeCreatedAt() {
        Merchant merchant = new Merchant.Builder()
                .setId(UUID.randomUUID())
                .setName("Test Merchant")
                .setEmail("merchant@example.com")
                .setStatus(MerchantStatus.ACTIVE)
                .setCreatedAt(Instant.now())
                .build();

        assertThat(merchant.getCreatedAt()).isNotNull();
    }
}