package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;

public record PaystackTransactionListResponse(boolean status, String message, List<PaystackTransactionResponse> data,
        Meta meta) {

    public record Meta(int total, int skipped, int perPage, int page) {
    }
}