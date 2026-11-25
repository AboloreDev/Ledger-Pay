package com.abolore.ledger_pay.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationFilter;
import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationProvider;
import com.abolore.ledger_pay.merchant_keys.security.MerchantApiKeyAuthenticationToken;
import com.abolore.ledger_pay.merchant_keys.service.MerchantApiKeyService;
import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;
import com.abolore.ledger_pay.merchants.entity.MerchantStatus;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class MerchantAuthenticationFilterTest {

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private FilterChain filterChain;

        private MerchantApiKeyAuthenticationFilter filter;

        private MockHttpServletRequest request;
        private MockHttpServletResponse response;

        @BeforeEach
        void setUp() {
                filter = new MerchantApiKeyAuthenticationFilter(authenticationManager);

                request = new MockHttpServletRequest();
                response = new MockHttpServletResponse();

                SecurityContextHolder.clearContext();
        }

        @Test
        void shouldAuthenticateValidApiKey() throws Exception {
                UUID merchantId = UUID.randomUUID();

                MerchantPrincipal principal = new MerchantPrincipal(
                                merchantId,
                                "merchant@example.com",
                                "Test Merchant",
                                MerchantStatus.ACTIVE,
                                Instant.now());

                MerchantApiKeyAuthenticationToken authenticatedToken = new MerchantApiKeyAuthenticationToken(principal);

                when(authenticationManager.authenticate(any(MerchantApiKeyAuthenticationToken.class)))
                                .thenReturn(authenticatedToken);

                request.addHeader("X-Merchant-API-Key", "lp_test_valid");

                filter.doFilter(request, response, filterChain);

                assertThat(SecurityContextHolder.getContext().getAuthentication())
                                .isSameAs(authenticatedToken);

                verify(filterChain).doFilter(request, response);
        }

        @Test
        void shouldRejectInvalidApiKey() throws Exception {
                when(authenticationManager.authenticate(any(MerchantApiKeyAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Invalid API key"));

                request.addHeader("X-Merchant-API-Key", "lp_test_invalid");

                filter.doFilter(request, response, filterChain);

                assertThat(response.getStatus()).isEqualTo(401);
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

                verifyNoInteractions(filterChain);
        }

        @Test
        void shouldNotAuthenticateMissingApiKey() throws Exception {
                filter.doFilter(request, response, filterChain);

                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

                verify(filterChain).doFilter(request, response);
        }

        @Test
        void shouldRejectBothAuthorizationAndApiKeyHeaders() throws Exception {
                request.addHeader("X-Merchant-API-Key", "lp_test_valid");
                request.addHeader("Authorization", "Bearer some-token");

                filter.doFilter(request, response, filterChain);

                assertThat(response.getStatus()).isEqualTo(400);

                verifyNoInteractions(filterChain);
                verifyNoInteractions(authenticationManager);
        }
}