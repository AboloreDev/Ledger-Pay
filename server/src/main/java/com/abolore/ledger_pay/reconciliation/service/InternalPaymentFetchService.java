package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;
import java.util.List;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

public class InternalPaymentFetchService {

    private final PaymentRepository paymentRepository;

    public InternalPaymentFetchService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    List<Payment> fetchPayment(Instant periodStart, Instant periodEnd) {

        return paymentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(periodStart, periodEnd);
    }
}
