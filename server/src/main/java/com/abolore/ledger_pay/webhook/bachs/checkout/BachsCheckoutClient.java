package com.abolore.ledger_pay.webhook.bachs.checkout;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BachsCheckoutClient {

    private final RestClient restClient;

    public BachsCheckoutClient(RestClient.Builder builder,
            @Value("${bachs.api.base-url}") String baseUrl,
            @Value("${bachs.api.key}") String apiKey) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey)
                .build();
    }

    public BachsCheckoutResponse createCheckout(
            UUID paymentId, String reference,
            BigDecimal amount, String currency) {

        Map<String, Object> requestBody = Map.of(
                "pricing", Map.of(
                        "amount", amount.toPlainString(),
                        "currency", currency),
                "reference", reference,
                "metadata", Map.of(
                        "payment_id", paymentId.toString()));

        return restClient.post()
                .uri("/checkout-sessions")
                .body(requestBody)
                .retrieve()
                .body(BachsCheckoutResponse.class);
    }
}