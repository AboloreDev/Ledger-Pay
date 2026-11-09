package com.abolore.ledger_pay.payment.provider.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaystackVerificationResponse(boolean status, String message, Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String status, String reference, Long amount, String currency) {
    }
}