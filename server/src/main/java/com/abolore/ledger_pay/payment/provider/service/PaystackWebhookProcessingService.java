package com.abolore.ledger_pay.payment.provider.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.provider.paystack.PaystackVerificationResponse;
import com.abolore.ledger_pay.payment.service.PaymentService;
import com.abolore.ledger_pay.webhook_event_audit.service.WebhookEventAuditService;

import jakarta.transaction.Transactional;

@Service
public class PaystackWebhookProcessingService {

    private final PaymentService paymentService;
    private final WebhookEventAuditService auditService;

    private static final String PROVIDER = "PAYSTACK";

    public PaystackWebhookProcessingService(PaymentService paymentService, WebhookEventAuditService auditService) {

        this.paymentService = paymentService;
        this.auditService = auditService;
    }

    @Transactional
    public void processSuccessfulPayment(PaystackVerificationResponse.Data transaction, UUID auditId,
            String deduplicationKey) {

        Payment payment = paymentService.getPaymentByReference(
                transaction.reference());

        if (!payment.getCurrency().equalsIgnoreCase(transaction.currency())) {
            throw new IllegalStateException("Payment currency mismatch");
        }

        BigDecimal verifiedAmount = BigDecimal.valueOf(transaction.amount()).movePointLeft(2);

        if (payment.getAmount().compareTo(verifiedAmount) != 0) {
            throw new IllegalStateException("Payment amount mismatch");
        }

        paymentService.markPaymentSucceeded(payment.getId());

        auditService.markProcessed(PROVIDER, deduplicationKey);
    }
}