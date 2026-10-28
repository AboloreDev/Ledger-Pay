package com.abolore.ledger_pay.payment.provider.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.OutboxEventStatus;
import com.abolore.ledger_pay.outbox.repository.OutboxEventRepository;
import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.repository.PaymentRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProviderInitiationResultService {

        private final PaymentRepository paymentRepository;
        private final OutboxEventRepository outboxEventRepository;

        public ProviderInitiationResultService(PaymentRepository paymentRepository,
                        OutboxEventRepository outboxEventRepository) {
                this.paymentRepository = paymentRepository;
                this.outboxEventRepository = outboxEventRepository;
        }

        @Transactional
        public void recordInitiationResult(UUID eventId, UUID claimToken, UUID paymentId, String providerReference,
                        String checkoutUrl) {

                Instant now = Instant.now();

                OutboxEvent event = outboxEventRepository.findByIdForUpdate(eventId)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Outbox event not found: " + eventId));

                if (event.getStatus() != OutboxEventStatus.PROCESSING
                                || !Objects.equals(event.getClaimToken(), claimToken)
                                || event.getLockedUntil() == null
                                || !event.getLockedUntil().isAfter(now)) {
                        throw new IllegalStateException(
                                        "Outbox event claim is no longer valid: " + eventId);
                }

                Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Payment not found: " + paymentId));

                if (payment.getProviderReference() != null
                                && !payment.getProviderReference().equals(providerReference)) {
                        throw new IllegalStateException(
                                        "Payment already has a different provider reference");
                }

                payment.setProviderReference(providerReference);
                payment.setCheckoutUrl(checkoutUrl);

                event.setStatus(OutboxEventStatus.PROCESSED);
                event.setProcessedAt(now);
                event.setLockedUntil(null);
                event.setClaimToken(null);
                event.setLastError(null);
                event.setUpdatedAt(now);

                paymentRepository.save(payment);
                outboxEventRepository.save(event);
        }
}