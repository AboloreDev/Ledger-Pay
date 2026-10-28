package com.abolore.ledger_pay.payment.exceptions;

public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String message) {
        super(message);
    }

    public DuplicatePaymentException(Throwable cause) {
        super(cause);
    }

    public DuplicatePaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
