package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionListResponse;

@Service
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
