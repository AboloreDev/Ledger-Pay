package com.abolore.ledger_pay.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.auth.dto.LoginResponse;

@Service
public class JWTService {

        private final JwtEncoder jwtEncoder;
        private final Clock clock;
        private final String issuer;
        private final long expirationSeconds;

        public JWTService(JwtEncoder jwtEncoder, Clock clock,
                        @Value("${app.jwt.issuer}") String issuer,
                        @Value("${app.jwt.expiration-seconds}") long expirationSeconds) {

                this.jwtEncoder = jwtEncoder;
                this.clock = clock;
                this.issuer = issuer;
                this.expirationSeconds = expirationSeconds;
        }

        public LoginResponse generateToken(Authentication authentication) {
                Instant issueAt = clock.instant();
                Instant expiresAt = issueAt.plusSeconds(expirationSeconds);

                List<String> roles = authentication.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .map(role -> role.replaceFirst("^ROLE_", ""))
                                .toList();

                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer(issuer).subject(authentication.getName())
                                .issuedAt(issueAt)
                                .expiresAt(expiresAt)
                                .claim("roles", roles).build();

                String token = jwtEncoder.encode(
                                JwtEncoderParameters.from(claims)).getTokenValue();

                return LoginResponse.bearer(token, expirationSeconds);
        }

}
