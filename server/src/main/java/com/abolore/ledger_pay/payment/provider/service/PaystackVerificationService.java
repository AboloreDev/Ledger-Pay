package com.abolore.ledger_pay.payment.provider.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.abolore.ledger_pay.payment.provider.paystack.PaystackVerificationResponse;

@Service
public class PaystackVerificationService {

    private final RestClient restClient;
    private final String secretKey;

    public PaystackVerificationService(RestClient.Builder restClientBuilder,
            @Value("${paystack.api.base-url}") String baseUrl,
            @Value("${paystack.api.secret-key}") String secretKey) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.secretKey = secretKey;
    }

    public PaystackVerificationResponse.Data verifyTransaction(String reference) {

        PaystackVerificationResponse response = restClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .retrieve()
                .body(PaystackVerificationResponse.class);

        if (response == null || !response.status() || response.data() == null) {
            throw new IllegalStateException(
                    "Paystack transaction verification failed");
        }

        PaystackVerificationResponse.Data transaction = response.data();

        if (!"success".equalsIgnoreCase(transaction.status())) {
            throw new IllegalStateException(
                    "Paystack transaction was not successful");
        }

        if (!reference.equals(transaction.reference())) {
            throw new IllegalStateException(
                    "Paystack transaction reference mismatch");
        }

        return transaction;
    }
}