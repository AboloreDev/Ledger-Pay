package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.abolore.ledger_pay.reconciliation.entity.PaystackProperties;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionClient;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionListResponse;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionResponse;

public class PaystackTransactionFetchService {

    private final PaystackProperties paystackProperties;
    private final PaystackTransactionClient paystackTransactionClient;

    public PaystackTransactionFetchService(PaystackProperties paystackProperties,
            PaystackTransactionClient paystackTransactionClient) {
        this.paystackProperties = paystackProperties;
        this.paystackTransactionClient = paystackTransactionClient;

    }

    public List<PaystackTransactionResponse> fetchAllTransactions(Instant from, Instant to) {

        List<PaystackTransactionResponse> transactions = new ArrayList<>();

        int page = 1;
        int perPage = paystackProperties.reconciliationRun().perPage();

        while (true) {
            PaystackTransactionListResponse response = paystackTransactionClient.fetchTransactions(from, to,
                    String.valueOf(page), String.valueOf(perPage));

            if (response == null || !response.status()) {
                throw new IllegalStateException(
                        "Unable to fetch Paystack transactions");
            }

            List<PaystackTransactionResponse> data = response.data();

            if (data == null || data.isEmpty()) {
                break;
            }

            transactions.addAll(data);

            page++;
        }

        return transactions;
    }

}
