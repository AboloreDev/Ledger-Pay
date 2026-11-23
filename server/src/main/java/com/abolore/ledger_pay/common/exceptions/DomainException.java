package com.abolore.ledger_pay.common.exceptions;

public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public static DomainException of(ErrorCode errorCode, String message) {
        return new DomainException(errorCode, message);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
