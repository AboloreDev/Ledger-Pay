package com.abolore.ledger_pay.merchant_keys.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.merchant_keys.service.MerchantApiKeyService;
import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;

@Service
public class MerchantApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final MerchantApiKeyService merchantApiKeyService;

    public MerchantApiKeyAuthenticationProvider(MerchantApiKeyService merchantApiKeyService) {
        this.merchantApiKeyService = merchantApiKeyService;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) {
        String rawKey = (String) authentication.getCredentials();

        MerchantPrincipal merchantPrincipal = merchantApiKeyService.authenticate(rawKey);

        return new MerchantApiKeyAuthenticationToken(merchantPrincipal);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MerchantApiKeyAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
