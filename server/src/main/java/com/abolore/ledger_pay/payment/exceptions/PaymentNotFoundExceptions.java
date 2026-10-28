package com.abolore.ledger_pay.payment.exceptions;

public class PaymentNotFoundExceptions extends RuntimeException {

    public PaymentNotFoundExceptions(String message) {
        super(message);
    }

    public PaymentNotFoundExceptions(Throwable cause) {
        super(cause);
    }

    public PaymentNotFoundExceptions(String message, Throwable cause) {
        super(message, cause);
    }

}
