package com.abolore.ledger_pay.payment.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.journal.entity.JournalEntryRequest;
import com.abolore.ledger_pay.journal.entity.JournalLineRequest;
import com.abolore.ledger_pay.journal.entity.JournalSides;
import com.abolore.ledger_pay.journal.service.JournalPostingService;
import com.abolore.ledger_pay.merchants.dto.MerchantPrincipal;
import com.abolore.ledger_pay.merchants.entity.Merchant;
import com.abolore.ledger_pay.outbox.OutboxEventFactory;
import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.PaymentInitiationPayload;
import com.abolore.ledger_pay.outbox.repository.OutboxEventRepository;
import com.abolore.ledger_pay.payment.dto.PaymentCreationResult;
import com.abolore.ledger_pay.payment.dto.PaymentResponse;
import com.abolore.ledger_pay.payment.entity.Payment;
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
        public PaymentCreationResult createPayment(UUID merchantId, String reference, BigDecimal amount,
                        String currency,
                        String customerEmail) {

                if (paymentRepository.existsByReference(reference)) {
                        throw DomainException.of(ErrorCode.DUPLICATE_PAYMENT,
                                        "Payment reference already exists");
                }

                Merchant merchant = paymentRepository.findMerchantById(merchantId)
                                .orElseThrow(() -> DomainException.of(ErrorCode.MERCHANT_NOT_FOUND, customerEmail));

                Instant now = Instant.now();
                Instant expiresAt = now.plus(Duration.ofMinutes(15));

                Payment payment = new Payment.Builder()
                                .setReference(reference)
                                .setAmount(amount)
                                .setCurrency(currency)
                                .setExpiresAt(expiresAt)
                                .setMerchant(merchant)
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
                                .orElseThrow(() -> DomainException.of(ErrorCode.PAYMENT_NOT_FOUND,
                                                "Payment not found: " + id));
        }

        // Scoped to merchant
        @Transactional(readOnly = true)
        public Payment getPaymentById(UUID paymentId, UUID merchantId) {
                return paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                                .orElseThrow(() -> DomainException.of(ErrorCode.PAYMENT_NOT_FOUND,
                                                "Payment not found for merchant: " + paymentId));
        }

        @Transactional(readOnly = true)
        public Payment getPaymentByReference(String reference) {
                return paymentRepository.findByReference(reference)
                                .orElseThrow(() -> DomainException.of(ErrorCode.PAYMENT_NOT_FOUND,
                                                "Payment not found: " + reference));
        }

        // Scoped to merchant
        @Transactional(readOnly = true)
        public Payment getPaymentByReference(String reference, UUID merchantId) {
                return paymentRepository.findByReferenceAndMerchant_Id(reference, merchantId)
                                .orElseThrow(() -> DomainException.of(ErrorCode.PAYMENT_NOT_FOUND,
                                                "Payment not found for merchant: " + reference));
        }

        // Return all merchant
        @Transactional(readOnly = true)
        public Page<PaymentResponse> getAllPaymentForMerchant(UUID merchantId, Pageable pageable) {

                Page<Payment> payment = paymentRepository.findAllByMerchant_Id(merchantId, pageable);

                return payment.map(PaymentResponse::from);
        }

        public Page<PaymentResponse> getAllPaymentForMerchant(Pageable pageable) {

                Page<Payment> payment = paymentRepository.findAllByMerchant_Id(pageable);

                return payment.map(PaymentResponse::from);
        }

        public Payment markPaymentSucceeded(UUID id) {
                Payment payment = paymentRepository.findById(id)
                                .orElseThrow(() -> DomainException.of(ErrorCode.PAYMENT_NOT_FOUND,
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

        public Payment markPaymentFailed(UUID id) {
                Payment payment = paymentRepository.findById(id)
                                .orElseThrow(() -> DomainException.of(ErrorCode.PAYMENT_NOT_FOUND,
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
