package com.abolore.ledger_pay.outbox.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.PaymentInitiationPayload;
import com.abolore.ledger_pay.payment.provider.PaymentInitiationRequest;
import com.abolore.ledger_pay.payment.provider.PaymentInitiationResult;
import com.abolore.ledger_pay.payment.provider.PaymentProvider;
import com.abolore.ledger_pay.payment.provider.service.ProviderInitiationResultService;

import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxEventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessingService.class);

    private final OutboxEventClaimService claimService;
    private final PaymentProvider paymentProvider;
    private final ProviderInitiationResultService resultService;
    private final ObjectMapper objectMapper;

    public OutboxEventProcessingService(
            OutboxEventClaimService claimService,
            PaymentProvider paymentProvider,
            ProviderInitiationResultService resultService,
            ObjectMapper objectMapper) {

        this.claimService = claimService;
        this.paymentProvider = paymentProvider;
        this.resultService = resultService;
        this.objectMapper = objectMapper;
    }

    public void processNextEvent() {

        Optional<OutboxEvent> optionalEvent = claimService.claimNextEvent();

        if (optionalEvent.isEmpty()) {
            return;
        }

        OutboxEvent event = optionalEvent.get();

        try {
            PaymentInitiationPayload payload = objectMapper.treeToValue(
                    event.getPayload(),
                    PaymentInitiationPayload.class);

            PaymentInitiationRequest request = new PaymentInitiationRequest(
                    payload.paymentId(),
                    payload.reference(),
                    payload.amount(),
                    payload.currency(),
                    payload.customerEmail());

            PaymentInitiationResult result = paymentProvider.initiatePayment(request);

            resultService.recordInitiationResult(
                    event.getId(),
                    event.getClaimToken(),
                    payload.paymentId(),
                    result.providerReference(),
                    result.checkoutUrl());

        } catch (Exception exception) {
            log.error(
                    "Failed to process outbox event {}. "
                            + "It can be reclaimed after its lease expires.",
                    event.getId(),
                    exception);
        }
    }
}
