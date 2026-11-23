package com.abolore.ledger_pay;

import java.time.Instant;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    public ProblemDetail handleDomainException(DomainException ex) {
        ErrorCode code = ex.getErrorCode();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                code.getStatus(),
                ex.getMessage());
        problem.setTitle(code.getTitle());
        problem.setProperty("errorCode", code.name());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
