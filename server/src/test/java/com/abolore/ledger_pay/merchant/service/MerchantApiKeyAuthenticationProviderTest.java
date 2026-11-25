package com.abolore.ledger_pay.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationProvider;
import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationToken;
import com.abolore.ledger_pay.merchant_keys.service.MerchantApiKeyService;
import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;
import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

@ExtendWith(MockitoExtension.class)
class MerchantApiKeyAuthenticationProviderTest {

    @Mock
    private MerchantApiKeyService apiKeyService;

    private MerchantApiKeyAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MerchantApiKeyAuthenticationProvider(apiKeyService);
    }

    @Test
    void shouldAuthenticateValidApiKey() {
        UUID merchantId = UUID.randomUUID();

        MerchantPrincipal principal = new MerchantPrincipal(
                merchantId,
                "merchant@example.com",
                "Test Merchant",
                MerchantStatus.ACTIVE,
                Instant.now());

        when(apiKeyService.authenticate("lp_test_valid"))
                .thenReturn(principal);

        MerchantApiKeyAuthenticationToken request = new MerchantApiKeyAuthenticationToken("lp_test_valid");

        Authentication result = provider.authenticate(request);

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo(principal);
    }

    @Test
    void shouldRejectInvalidApiKey() {
        when(apiKeyService.authenticate("lp_test_invalid"))
                .thenThrow(DomainException.of(ErrorCode.MERCHANT_INVALID_API_KEY, "Invalid API key"));

        MerchantApiKeyAuthenticationToken request = new MerchantApiKeyAuthenticationToken("lp_test_invalid");

        assertThatThrownBy(() -> provider.authenticate(request))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldSupportMerchantApiKeyAuthenticationToken() {
        assertThat(provider.supports(MerchantApiKeyAuthenticationToken.class))
                .isTrue();
    }
}