package com.abolore.ledger_pay.merchant_keys.security;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;

public class MerchantApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private Object principal;
    private String credentials;

    // Unauthenticated Requests
    public MerchantApiKeyAuthenticationToken(String apiKey) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.credentials = apiKey;
        setAuthenticated(false);
    }

    // Authenticated Merchant
    public MerchantApiKeyAuthenticationToken(MerchantPrincipal principal) {
        super(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")));
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return credentials;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return principal;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
