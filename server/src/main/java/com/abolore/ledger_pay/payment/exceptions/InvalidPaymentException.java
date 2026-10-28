package com.abolore.ledger_pay.payment.exceptions;

public class InvalidPaymentException extends RuntimeException {

    public InvalidPaymentException(String message) {
        super(message);
    }

    public InvalidPaymentException(Throwable cause) {
        super(cause);
    }

    public InvalidPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
