package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.reconciliation.entity.PaystackProperties;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionListResponse;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionResponse;
import com.abolore.ledger_pay.reconciliation.service.PaystackTransactionClient;

@Service
public class PaystackTransactionFetchService {

    private final PaystackProperties paystackProperties;
    private final PaystackTransactionClient paystackTransactionClient;
    private static final Logger logger = LoggerFactory.getLogger(PaystackTransactionFetchService.class);

    public PaystackTransactionFetchService(PaystackProperties paystackProperties,
            PaystackTransactionClient paystackTransactionClient) {
        this.paystackProperties = paystackProperties;
        this.paystackTransactionClient = paystackTransactionClient;

    }

    public List<PaystackTransactionResponse> fetchAllTransactions(Instant from, Instant to) {

        List<PaystackTransactionResponse> transactions = new ArrayList<>();

        int page = 1;
        logger.info("Fetching per page");
        // int perPage = paystackProperties.reconciliationRun().perPage();
        int perPage = 100;
        logger.info(String.valueOf(perPage));

        while (true) {
            logger.info("Starting transaction client");
            PaystackTransactionListResponse response = paystackTransactionClient.fetchTransactions(from, to,
                    String.valueOf(page), String.valueOf(perPage));

            if (response == null || !response.status()) {
                throw DomainException.of(ErrorCode.PROVIDER_FETCH_ERROR,
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
