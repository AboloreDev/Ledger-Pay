package com.abolore.ledger_pay.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKey;
import com.abolore.ledger_pay.merchant_keys.entity.MerchantApiKeyStatus;
import com.abolore.ledger_pay.merchant_keys.repository.MerchantApiKeyRepository;
import com.abolore.ledger_pay.merchant_keys.service.MerchantApiKeyService;
import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

@ExtendWith(MockitoExtension.class)
class MerchantApiKeyServiceTest {

    @Mock
    private MerchantApiKeyRepository apiKeyRepository;

    @InjectMocks
    private MerchantApiKeyService apiKeyService;

    @Test
    void shouldGenerateApiKey() {
        Merchant merchant = new Merchant.Builder()
                .setId(UUID.randomUUID())
                .setName("Test Merchant").setEmail("merchant@exaple.com")
                .setStatus(MerchantStatus.ACTIVE).setCreatedAt(Instant.now()).build();

        String apiKey = apiKeyService.generateKey(merchant);

        assertThat(apiKey).isNotBlank();
        assertThat(apiKey).startsWith("lp_test_");
    }

    @Test
    void shouldStoreHashedKeyInsteadOfRawKey() {
        Merchant merchant = new Merchant.Builder()
                .setId(UUID.randomUUID())
                .setName("Test Merchant").setEmail("merchant@exaple.com")
                .setStatus(MerchantStatus.ACTIVE).setCreatedAt(Instant.now()).build();

        apiKeyService.generateKey(merchant);

        ArgumentCaptor<MerchantApiKey> captor = ArgumentCaptor.forClass(MerchantApiKey.class);

        verify(apiKeyRepository).save(captor.capture());

        MerchantApiKey savedKey = captor.getValue();

        assertThat(savedKey.getKeyHash()).isNotBlank();
        assertThat(savedKey.getKeyHash()).doesNotStartWith("lp_test_");
    }

    @Test
    void shouldGenerateDifferentKeysForDifferentRequests() {
        Merchant merchant = new Merchant.Builder()
                .setId(UUID.randomUUID())
                .setName("Test Merchant").setEmail("merchant@exaple.com")
                .setStatus(MerchantStatus.ACTIVE).setCreatedAt(Instant.now()).build();

        String firstKey = apiKeyService.generateKey(merchant);
        String secondKey = apiKeyService.generateKey(merchant);

        assertThat(firstKey).isNotEqualTo(secondKey);
    }

}