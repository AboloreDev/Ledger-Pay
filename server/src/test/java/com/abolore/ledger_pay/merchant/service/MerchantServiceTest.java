package com.abolore.ledger_pay.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.merchant_keys.service.MerchantApiKeyService;
import com.abolore.ledger_pay.merchants.dto.MerchantRegistrationRequest;
import com.abolore.ledger_pay.merchants.dto.MerchantRegistrationResponse;
import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.merchants.repository.MerchantRepository;
import com.abolore.ledger_pay.merchants.service.MerchantRegistrationService;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantApiKeyService apiKeyService;

    @InjectMocks
    private MerchantRegistrationService merchantRegistrationService;

    @Test
    void shouldRegisterMerchant() {
        when(merchantRepository.existsByEmail("merchant@example.com"))
                .thenReturn(false);

        when(merchantRepository.save(any(Merchant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(apiKeyService.generateKey(any()))
                .thenReturn("lp_test_generated_key");

        MerchantRegistrationRequest registrationRequest = new MerchantRegistrationRequest("Test Merchant",
                "merchant@example.com");

        MerchantRegistrationResponse result = merchantRegistrationService.register(registrationRequest);

        assertThat(result).isNotNull();
        assertThat(result.apiKey()).isEqualTo("lp_test_generated_key");

        verify(merchantRepository).save(any(Merchant.class));
        verify(apiKeyService).generateKey(any());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(merchantRepository.existsByEmail("merchant@example.com"))
                .thenReturn(true);

        MerchantRegistrationRequest registrationRequest = new MerchantRegistrationRequest("Test Merchant",
                "merchant@example.com");

        assertThatThrownBy(() -> merchantRegistrationService.register(registrationRequest))
                .isInstanceOf(DomainException.class);

        verify(merchantRepository, never()).save(any());
        verify(apiKeyService, never()).generateKey(any());
    }
}
