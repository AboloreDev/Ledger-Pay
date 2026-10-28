package com.abolore.ledger_pay.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.entity.JournalLineRequest;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.service.JournalPostingService;
import com.abolore.ledger_pay.outbox.OutboxEventFactory;
import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.PaymentInitiationPayload;
import com.abolore.ledger_pay.outbox.repository.OutboxEventRepository;
import com.abolore.ledger_pay.payment.dto.PaymentCreationResult;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.exceptions.DuplicatePaymentException;
import com.abolore.ledger_pay.payment.exceptions.PaymentNotFoundExceptions;
import com.abolore.ledger_pay.payment.provider.PaymentProvider;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

@Service
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final JournalPostingService journalPostingService;
        private final UUID providerClearingAccountId;
        private final UUID merchantPayableAccountId;
        private final PaymentProvider paymentProvider;
        private final OutboxEventRepository outboxEventRepository;
        private final OutboxEventFactory outboxEventFactory;

        public PaymentService(
                        PaymentRepository paymentRepository,
                        JournalPostingService journalPostingService,
                        @Value("${ledger.accounts.provider-clearing-id}") UUID providerClearingAccountId,
                        @Value("${ledger.accounts.merchant-payable-id}") UUID merchantPayableAccountId,
                        PaymentProvider paymentProvider, OutboxEventRepository outboxEventRepository,
                        OutboxEventFactory outboxEventFactory) {
                this.paymentRepository = paymentRepository;
                this.journalPostingService = journalPostingService;
                this.providerClearingAccountId = providerClearingAccountId;
                this.merchantPayableAccountId = merchantPayableAccountId;
                this.paymentProvider = paymentProvider;
                this.outboxEventRepository = outboxEventRepository;
                this.outboxEventFactory = outboxEventFactory;
        }

        @Transactional
        public PaymentCreationResult createPayment(String reference, BigDecimal amount, String currency,
                        String customerEmail) {

                if (paymentRepository.existsByReference(reference)) {
                        throw new DuplicatePaymentException(
                                        "Payment reference already exists: " + reference);
                }

                Payment payment = new Payment.Builder()
                                .setReference(reference)
                                .setAmount(amount)
                                .setCurrency(currency)
                                .build();

                payment = paymentRepository.save(payment);

                PaymentInitiationPayload payload = new PaymentInitiationPayload(
                                payment.getId(),
                                payment.getReference(),
                                payment.getAmount(),
                                payment.getCurrency(),
                                customerEmail);

                OutboxEvent event = outboxEventFactory.paymentInitiationRequested(payload);

                outboxEventRepository.save(event);

                return new PaymentCreationResult(payment, null);
        }

        @Transactional(readOnly = true)
        public Payment getPaymentById(UUID id) {
                return paymentRepository.findById(id)
                                .orElseThrow(() -> new PaymentNotFoundExceptions(
                                                "Payment not found: " + id));
        }

        @Transactional(readOnly = true)
        public Payment getPaymentByReference(String reference) {
                return paymentRepository.findByReference(reference)
                                .orElseThrow(() -> new PaymentNotFoundExceptions(
                                                "Payment not found: " + reference));
        }

        @Transactional
        public Payment markPaymentSucceeded(UUID id) {
                Payment payment = paymentRepository.findById(id)
                                .orElseThrow(() -> new PaymentNotFoundExceptions(
                                                "Payment not found: " + id));

                // Reject invalid state transitions before posting anything.
                payment.markSucceeded();

                // Post a journal entry on succeeding state
                JournalEntryRequest journalEntry = new JournalEntryRequest(
                                "PAYMENT-" + payment.getReference(),
                                "PAYMENT",
                                payment.getId(),
                                payment.getId(),
                                requestLines(payment));

                journalPostingService.postJournalEntry(journalEntry);

                return paymentRepository.save(payment);
        }

        @Transactional
        public Payment markPaymentFailed(UUID id) {
                Payment payment = paymentRepository.findById(id)
                                .orElseThrow(() -> new PaymentNotFoundExceptions(
                                                "Payment not found: " + id));

                payment.markFailed();

                return paymentRepository.save(payment);
        }

        // Helper method for payment list
        private List<JournalLineRequest> requestLines(Payment payment) {
                return new ArrayList<>(List.of(
                                new JournalLineRequest(
                                                providerClearingAccountId,
                                                payment.getCurrency(),
                                                JournalSides.DEBIT,
                                                payment.getAmount()),

                                new JournalLineRequest(
                                                merchantPayableAccountId,
                                                payment.getCurrency(),
                                                JournalSides.CREDIT,
                                                payment.getAmount())));
        }

}
