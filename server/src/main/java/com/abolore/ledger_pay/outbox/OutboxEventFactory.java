package com.abolore.ledger_pay.outbox;

import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.OutboxEventType;
import com.abolore.ledger_pay.outbox.entity.PaymentInitiationPayload;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEventFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent paymentInitiationRequested(PaymentInitiationPayload payload) {

        return OutboxEvent.create(
                payload.paymentId(),
                OutboxEventType.PAYMENT_INITIATION_REQUESTED.name(),
                objectMapper.valueToTree(payload));
    }
}
