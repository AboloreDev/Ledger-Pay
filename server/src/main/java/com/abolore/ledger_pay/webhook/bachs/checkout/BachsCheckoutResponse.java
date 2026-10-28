package com.abolore.ledger_pay.webhook.bachs.checkout;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BachsCheckoutResponse(
        String checkout_id,
        String checkout_url,
        String status) {
}