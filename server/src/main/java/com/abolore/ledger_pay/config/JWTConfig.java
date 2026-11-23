package com.abolore.ledger_pay.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JWTConfig {

    @Bean
    public JwtEncoder jwtEncoder(
            @Value("${app.jwt.secret}") String secret) {

        SecretKey sk = new SecretKeySpec(
                Base64.getDecoder().decode(secret),
                "HmacSHA256");

        return new NimbusJwtEncoder(new ImmutableSecret<>(sk));
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer) {

        SecretKey sk = new SecretKeySpec(
                Base64.getDecoder().decode(secret), "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(sk)
                .macAlgorithm(MacAlgorithm.HS256).build();

        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));

        return decoder;
    }
}
