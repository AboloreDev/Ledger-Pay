package com.abolore.ledger_pay;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.abolore.ledger_pay.journal.exceptions.DuplicateSourceEventException;
import com.abolore.ledger_pay.journal.exceptions.JournalErrorResponse;
import com.abolore.ledger_pay.payment.exceptions.DuplicatePaymentException;
import com.abolore.ledger_pay.payment.exceptions.InvalidPaymentException;
import com.abolore.ledger_pay.payment.exceptions.PaymentErrorResponse;
import com.abolore.ledger_pay.payment.exceptions.PaymentNotFoundExceptions;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(DuplicateSourceEventException.class)
    public ResponseEntity<JournalErrorResponse> handleException(DuplicateSourceEventException exception) {

        JournalErrorResponse errorResponse = new JournalErrorResponse();

        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<PaymentErrorResponse> handleException(InvalidPaymentException exception) {

        PaymentErrorResponse errorResponse = new PaymentErrorResponse();

        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentNotFoundExceptions.class)
    public ResponseEntity<PaymentErrorResponse> handleException(PaymentNotFoundExceptions exception) {

        PaymentErrorResponse errorResponse = new PaymentErrorResponse();

        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<PaymentErrorResponse> handleException(DuplicatePaymentException exception) {

        PaymentErrorResponse errorResponse = new PaymentErrorResponse();

        errorResponse.setStatus(HttpStatus.CONFLICT.value());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
