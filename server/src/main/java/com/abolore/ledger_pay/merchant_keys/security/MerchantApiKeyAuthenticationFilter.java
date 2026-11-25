package com.abolore.ledger_pay.merchant_keys.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MerchantApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Merchant-API-Key";

    private final AuthenticationManager authenticationManager;

    public MerchantApiKeyAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(API_KEY_HEADER);

        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Prevent ambiguous Authentication Identity
        if (request.getHeader("Authorization") != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot use both API Key and Authorization header");
            return;
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new MerchantApiKeyAuthenticationToken(header));

            SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);
        } catch (AuthenticationException exception) {

            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid merchant API key");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
