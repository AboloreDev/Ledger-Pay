package com.abolore.ledger_pay.payment.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.service.JournalPostingService;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.payment.exceptions.PaymentNotFoundExceptions;
import com.abolore.ledger_pay.payment.provider.PaymentVerificationResult;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

import jakarta.transaction.Transactional;

@Service
// @RequiredArgsConstructor
public class PaymentFinalizationService {

    private final PaymentRepository paymentRepository;
    private final JournalPostingService journalPostingService;

    public PaymentFinalizationService(PaymentRepository paymentRepository,
            JournalPostingService journalPostingService) {
        this.paymentRepository = paymentRepository;
        this.journalPostingService = journalPostingService;
    }

    @Transactional
    public void processVerification(UUID paymentId, PaymentVerificationResult result) {

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new PaymentNotFoundExceptions("Payment not found"));

        // Another process may have completed it while we verified.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        validateVerification(payment, result);

        if (!"success".equalsIgnoreCase(result.status())) {
            return;
        }

        Instant paidAt = result.paidAt();

        if (paidAt == null) {
            throw new IllegalStateException(
                    "Successful provider transaction has no paid_at timestamp");
        }

        boolean latePayment = paidAt.isAfter(payment.getExpiresAt());

        // payment.markSucceeded(paidAt, latePayment);

        journalPostingService.postJournalEntry(buildPaymentJournalRequest(payment));

        paymentRepository.save(payment);
    }

    @Transactional
    public void expirePayment(UUID paymentId, Instant now) {

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new PaymentNotFoundExceptions("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Instant expiryWithGrace = payment.getExpiresAt().plus(Duration.ofMinutes(2));

        if (now.isBefore(expiryWithGrace)) {
            return;
        }

        // payment.markExpired();
        paymentRepository.save(payment);
    }

    private void validateVerification(Payment payment, PaymentVerificationResult result) {

        if (!payment.getReference().equals(result.providerReference())) {
            throw new IllegalStateException("Payment reference mismatch");
        }

        if (!payment.getCurrency().equalsIgnoreCase(result.currency())) {
            throw new IllegalStateException("Payment currency mismatch");
        }

        long expectedAmount = payment.getAmount()
                .movePointRight(2)
                .longValueExact();

        if (expectedAmount != result.amountInMinorUnits()) {
            throw new IllegalStateException("Payment amount mismatch");
        }
    }

    private JournalEntryRequest buildPaymentJournalRequest(Payment payment) {
        // Reuse your existing journal request construction and requestLines().
        // Do not create a second implementation of your ledger logic.
        throw new UnsupportedOperationException("Connect existing journal builder");
    }
}