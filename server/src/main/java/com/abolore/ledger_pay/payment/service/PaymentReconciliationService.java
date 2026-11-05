package com.abolore.ledger_pay.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.payment.exceptions.PaymentNotFoundExceptions;
import com.abolore.ledger_pay.payment.provider.PaymentProvider;
import com.abolore.ledger_pay.payment.provider.PaymentVerificationResult;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

@Service
// @RequiredArgsConstructor
public class PaymentReconciliationService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentFinalizationService finalizationService;

    public PaymentReconciliationService(PaymentRepository paymentRepository, PaymentProvider paymentProvider,
            PaymentFinalizationService finalizationService) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
        this.finalizationService = finalizationService;
    }

    public void reconcile(UUID paymentId) {

        // Stage A: Read payment without acquiring a row lock.
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundExceptions("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        // Stage B: Verify with Paystack.
        PaymentVerificationResult result = paymentProvider.verifyPayment(payment.getReference());

        // Stage C: Finalize in a separate transaction.
        finalizationService.processVerification(paymentId, result);
    }
}