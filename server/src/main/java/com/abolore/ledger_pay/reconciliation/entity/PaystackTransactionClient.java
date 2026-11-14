package com.abolore.ledger_pay.reconciliation.entity;

import java.time.Instant;

import org.springframework.web.client.RestClient;

public class PaystackTransactionClient {

    private final RestClient restClient;

    public PaystackTransactionClient(RestClient paystackRestClient) {
        this.restClient = paystackRestClient;
    }

    public PaystackTransactionListResponse fetchTransactions(Instant from, Instant to, String page, String perPage) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/transaction")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                        .queryParam("page", page)
                        .queryParam("perPage", perPage)
                        .build())
                .retrieve()
                .body(PaystackTransactionListResponse.class);
    }
}
