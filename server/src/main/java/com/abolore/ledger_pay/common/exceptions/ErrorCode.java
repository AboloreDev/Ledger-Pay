package com.abolore.ledger_pay.common.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Payment Errors
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment Not Found"),
    INVALID_PAYMENT(HttpStatus.BAD_REQUEST, "Invalid Payment Request"),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "Duplicate Payment Reference"),
    PROVIDER_DATA_INCONSISTENCY(HttpStatus.BAD_GATEWAY, "Bad Gateway"),

    // Ledger Errors
    DUPLICATE_LEDGER_POSTING(HttpStatus.CONFLICT, "Duplicate Ledger Posting"),
    LEDGER_NOT_FOUND(HttpStatus.NOT_FOUND, "Ledger not found"),

    // Account Errors
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account Not Found"),
    INSUFFICIENT_FUNDS(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient Funds"),

    // Reconciliation Errors
    OVERLAPPING_RECONCILIATION_RUN(HttpStatus.CONFLICT, "Overlapping Reconciliation Run"),

    // Journal Errors
    DUPLICATE_SOURCE_POSTING(HttpStatus.CONFLICT, "Duplicate ledger source events"),
    JOURNAL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Journal posting Request "),
    UNBALANCED_JOURNAL_ENTRY(HttpStatus.UNPROCESSABLE_ENTITY, "Unbalanced Journal Entry"),
    JOURNAL_ENTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "Journal not found"),

    // Webhook Errors
    WEBHOOK_PROCESSING_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "Webhook Processing failed"),
    WEBHOOK_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Webhook not found"),

    // Outbox Errors
    OUTBOX_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Outbox Event not found"),

    // External fetch error
    PROVIDER_FETCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),

    // Reconciliation Error
    RECONCILIATION_PROCESSING_CONFLICT(HttpStatus.CONFLICT, "A run is in progress"),

    // Admin Erros
    ADMIN_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Fields must be complete"),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "Email not found"),

    // MERCHANT
    MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Merchant Not Found"),
    MERCHANT_EXISTS(HttpStatus.CONFLICT, "Merchant already exists"),
    MERCHANT_INVALID_API_KEY(HttpStatus.BAD_REQUEST, "Invalid API Key"),
    INVALID_MERCHANT_STATUS(HttpStatus.BAD_GATEWAY, "Invalid Status"),

    // Hashing Error
    INCORRECT_HASHING_ALGORITHM(HttpStatus.FORBIDDEN, "SHA-256 algorithm unavailable");

    private final HttpStatus status;
    private final String title;

    ErrorCode(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
