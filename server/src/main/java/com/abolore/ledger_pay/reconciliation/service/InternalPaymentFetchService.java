package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

@Service
public class InternalPaymentFetchService {

    private final PaymentRepository paymentRepository;

    public InternalPaymentFetchService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> fetchPayment(Instant periodStart, Instant periodEnd) {

        return paymentRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(periodStart, periodEnd);
    }

    public List<Payment> fetchPaymentsByIds(Collection<UUID> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            return List.of();
        }

        return paymentRepository.findAllByIdIn(paymentIds);
    }
}
