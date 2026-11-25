package com.abolore.ledger_pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.abolore.ledger_pay.auth.service.AdminUserDetailsService;
import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationProvider;

@Configuration
public class AuthenticationConfig {

    @Bean
    public AuthenticationProvider authenticationProvider(
            AdminUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {

        return new ProviderManager(authenticationProvider);
    }

    @Bean("merchantAuthenticationManager")
    public AuthenticationManager merchantAuthenticationManager(MerchantApiKeyAuthenticationProvider provider) {

        return new ProviderManager(provider);
    }

}
