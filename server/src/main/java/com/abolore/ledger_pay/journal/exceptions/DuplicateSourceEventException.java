package com.abolore.ledger_pay.journal.exceptions;

public class DuplicateSourceEventException extends RuntimeException {

    public DuplicateSourceEventException(String message) {
        super(message);
    }

    public DuplicateSourceEventException(Throwable cause) {
        super(cause);
    }

    public DuplicateSourceEventException(String message, Throwable cause) {
        super(message, cause);
    }

}
