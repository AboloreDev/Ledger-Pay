package com.abolore.ledger_pay.payment.provider.paystack;

import java.math.RoundingMode;
import java.time.Instant;

import com.abolore.ledger_pay.payment.provider.PaymentInitiationRequest;
import com.abolore.ledger_pay.payment.provider.PaymentInitiationResult;
import com.abolore.ledger_pay.payment.provider.PaymentProvider;
import com.abolore.ledger_pay.payment.provider.PaymentVerificationResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaystackPaymentProvider implements PaymentProvider {

        private final RestClient restClient;

        public PaystackPaymentProvider(RestClient.Builder builder, @Value("${paystack.api.base-url}") String baseUrl,
                        @Value("${paystack.api.secret-key}") String secretKey) {

                this.restClient = builder
                                .baseUrl(baseUrl)
                                .defaultHeader(
                                                "Authorization",
                                                "Bearer " + secretKey)
                                .build();
        }

        @Override
        public PaymentInitiationResult initiatePayment(PaymentInitiationRequest request) {

                long amountInMinorUnits = request
                                .amount()
                                .movePointRight(2)
                                .setScale(0, RoundingMode.UNNECESSARY)
                                .longValueExact();

                PaystackInitializeRequest body = new PaystackInitializeRequest(
                                request.customerEmail(),
                                amountInMinorUnits,
                                request.currency(),
                                request.reference());

                PaystackInitializeResponse response = restClient.post()
                                .uri("/transaction/initialize")
                                .body(body)
                                .retrieve()
                                .body(PaystackInitializeResponse.class);

                if (response == null || !response.status() || response.data() == null) {
                        throw new IllegalStateException(
                                        "Paystack did not confirm transaction initialization");
                }

                return new PaymentInitiationResult(response.data().reference(), response.data().authorizationUrl());
        }

        @Override
        public PaymentVerificationResult verifyPayment(String providerReference) {

                PaystackVerifyResponse response = restClient.get()
                                .uri("/transaction/verify/{reference}", providerReference)
                                .retrieve()
                                .body(PaystackVerifyResponse.class);

                if (response == null || !response.status() || response.data() == null) {
                        throw new IllegalStateException(
                                        "Paystack verification request was unsuccessful");
                }

                PaystackTransactionData data = response.data();

                return new PaymentVerificationResult(
                                data.reference(),
                                data.status(),
                                data.amount(),
                                data.currency(), data.paidAt());
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record PaystackInitializeRequest(
                        String email,
                        long amount,
                        String currency,
                        String reference) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record PaystackInitializeResponse(
                        boolean status,
                        String message,
                        PaystackInitializeData data) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record PaystackInitializeData(
                        @JsonProperty("authorization_url") String authorizationUrl, String reference) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record PaystackVerifyResponse(
                        boolean status,
                        String message,
                        PaystackTransactionData data) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record PaystackTransactionData(
                        String status,
                        String reference,
                        long amount,
                        String currency,
                        @JsonProperty("paid_at") Instant paidAt) {
        }
}