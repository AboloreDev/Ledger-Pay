package com.abolore.ledger_pay.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                        JwtAuthenticationConverter jwtAuthenticationConverter,
                        @Qualifier("merchantAuthenticationManager") AuthenticationManager merchantAuthenticationManager)
                        throws Exception {

                httpSecurity
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(
                                                new MerchantApiKeyAuthenticationFilter(merchantAuthenticationManager),
                                                BearerTokenAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/merchants/register",
                                                                "/api/v1/merchants/me",
                                                                "/api/v1/webhooks/**",
                                                                "/api/v1/ledger-pay/actuator/health")
                                                .permitAll()

                                                // Global reconciliation remains admin-only
                                                .requestMatchers(
                                                                "/api/v1/reconciliations",
                                                                "/api/v1/reconciliations/**")
                                                .hasRole("ADMIN")

                                                // Merchant-specific reconciliation
                                                .requestMatchers(
                                                                "/api/v1/merchant/reconciliations/**")
                                                .hasRole("MERCHANT")

                                                // Shared payment and journal endpoints
                                                .requestMatchers(
                                                                "/api/v1/payments/**",
                                                                "/api/v1/journals/**")
                                                .hasAnyRole("ADMIN", "MERCHANT")

                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(
                                                oauth2 -> oauth2.jwt(
                                                                jwt -> jwt.jwtAuthenticationConverter(
                                                                                jwtAuthenticationConverter)));

                return httpSecurity.build();
        }

}
