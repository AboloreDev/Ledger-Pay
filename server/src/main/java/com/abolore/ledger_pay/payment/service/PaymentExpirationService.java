package com.abolore.ledger_pay.payment.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

@Service
// @RequiredArgsConstructor
public class PaymentExpirationService {

    private final PaymentRepository paymentRepository;
    private final PaymentReconciliationService reconciliationService;
    private final PaymentFinalizationService finalizationService;

    public PaymentExpirationService(PaymentRepository paymentRepository,
            PaymentReconciliationService reconciliationService, PaymentFinalizationService finalizationService) {
        this.paymentRepository = paymentRepository;
        this.reconciliationService = reconciliationService;
        this.finalizationService = finalizationService;
    }

    public void processExpiredPayments() {

        Instant now = Instant.now();
        Instant graceCutoff = now.minus(Duration.ofMinutes(2));

        List<Payment> paymentsForReconciliation = paymentRepository.findPaymentsForReconciliation(PaymentStatus.PENDING,
                now, graceCutoff);

        for (Payment payment : paymentsForReconciliation) {
            try {
                reconciliationService.reconcile(payment.getId());
            } catch (Exception exception) {
                // Log the failure. Retry on a later scheduler run.
            }
        }

        List<Payment> paymentsReadyForExpiry = paymentRepository.findPaymentsReadyForExpiry(PaymentStatus.PENDING,
                graceCutoff);

        for (Payment payment : paymentsReadyForExpiry) {
            finalizationService.expirePayment(payment.getId(), now);
        }
    }
}