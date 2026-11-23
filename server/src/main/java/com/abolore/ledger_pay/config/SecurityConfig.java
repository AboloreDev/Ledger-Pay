package com.abolore.ledger_pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()

                        .requestMatchers("/api/v1/webhooks/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/actuator/health")
                        .permitAll()

                        .requestMatchers("/actuator/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/reconciliations/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/journals/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return httpSecurity.build();
    }
}
