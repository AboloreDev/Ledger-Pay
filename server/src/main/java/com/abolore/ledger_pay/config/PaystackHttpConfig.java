package com.abolore.ledger_pay.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.abolore.ledger_pay.reconciliation.entity.PaystackProperties;

@Configuration
@EnableConfigurationProperties(PaystackProperties.class)
public class PaystackHttpConfig {

    @Bean
    public RestClient paystackRestClient(PaystackProperties properties) {

        return RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiSecretKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}