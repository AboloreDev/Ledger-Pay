package com.abolore.ledger_pay.reconciliation.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.reconciliation.entity.PaymentComparisonResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingResult;
import com.abolore.ledger_pay.reconciliation.entity.PaymentMatchingSummary;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionResponse;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRecordType;

public class PaymentMatchingService {

    private final PaystackTransactionFetchService paystackTransactionFetchService;
    private final PaymentComparisonService paymentComparisonService;
    private final InternalPaymentFetchService internalPaymentFetchService;

    public PaymentMatchingService(PaystackTransactionFetchService paystackTransactionFetchService,
            InternalPaymentFetchService internalPaymentFetchService,
            PaymentComparisonService paymentComparisonService) {
        this.paystackTransactionFetchService = paystackTransactionFetchService;
        this.internalPaymentFetchService = internalPaymentFetchService;
        this.paymentComparisonService = paymentComparisonService;
    }

    public PaymentMatchingSummary match(Instant periodStart, Instant periodEnd) {

        List<PaystackTransactionResponse> providerTransactions = paystackTransactionFetchService
                .fetchAllTransactions(periodStart, periodEnd);

        List<Payment> internalPayment = internalPaymentFetchService.fetchPayment(periodStart, periodEnd);

        Map<String, List<PaystackTransactionResponse>> providerByReference = new HashMap<>();

        for (PaystackTransactionResponse transaction : providerTransactions) {
            providerByReference.computeIfAbsent(transaction.reference(), k -> new java.util.ArrayList<>())
                    .add(transaction);
        }

        Map<String, List<Payment>> paymentByReference = new HashMap<>();

        for (Payment payment : internalPayment) {
            paymentByReference.computeIfAbsent(payment.getReference(), k -> new java.util.ArrayList<>())
                    .add(payment);
        }

        Set<UUID> pairedPaymentID = new HashSet<>();
        List<PaymentMatchingResult> matchResult = new ArrayList<>();

        int matchedCount = 0;
        int discepantCount = 0;
        int unmatchedInternalCount = 0;
        int unmatchedPaystackCount = 0;

        for (Map.Entry<String, List<PaystackTransactionResponse>> entry : providerByReference.entrySet()) {

            String reference = entry.getKey();
            List<PaystackTransactionResponse> providerMatches = entry.getValue();

            // Handle missing/blank references (if null keys were grouped into the map)
            if (reference == null || reference.isBlank()) {
                for (PaystackTransactionResponse ignored : providerMatches) {
                    matchResult.add(new PaymentMatchingResult(
                            ReconciliationRecordType.PAYSTACK_TRANSACTION,
                            reference,
                            null,
                            false,
                            List.of("MISSING_REFERENCE")));

                    unmatchedPaystackCount++;
                }
                continue;
            }

            // Flag duplicate Paystack transactions for this reference ONCE
            if (providerMatches.size() > 1) {
                matchResult.add(new PaymentMatchingResult(
                        ReconciliationRecordType.PAYSTACK_TRANSACTION,
                        reference,
                        null,
                        false,
                        List.of("DUPLICATE_PROVIDER_REFERENCE")));

                unmatchedPaystackCount += providerMatches.size(); // Counts all raw provider rows involved
                continue;
            }

            // Safe to unwrap the single Paystack transaction
            PaystackTransactionResponse transaction = providerMatches.get(0);

            List<Payment> internalMatches = paymentByReference.get(reference);

            if (internalMatches == null || internalMatches.isEmpty()) {
                matchResult.add(new PaymentMatchingResult(
                        ReconciliationRecordType.PAYSTACK_TRANSACTION,
                        reference,
                        null,
                        false,
                        List.of("MISSING_INTERNAL_PAYMENT")));

                unmatchedPaystackCount++;
                continue;
            }

            if (internalMatches.size() > 1) {
                matchResult.add(new PaymentMatchingResult(
                        ReconciliationRecordType.PAYSTACK_TRANSACTION,
                        reference,
                        null,
                        false,
                        List.of("DUPLICATE_INTERNAL_REFERENCE")));

                unmatchedPaystackCount++;
                continue;
            }

            Payment payment = internalMatches.get(0);
            pairedPaymentID.add(payment.getId());

            PaymentComparisonResult comparison = paymentComparisonService.compare(transaction, payment);

            matchResult.add(new PaymentMatchingResult(
                    ReconciliationRecordType.PAYSTACK_TRANSACTION,
                    reference,
                    payment.getId(),
                    comparison.matched(),
                    comparison.discrepancies()));

            if (comparison.matched()) {
                matchedCount++;
            } else {
                discepantCount++;
            }

        }

        // Catch internal payments missing from Paystack
        for (Payment payment : internalPayment) {
            if (!pairedPaymentID.contains(payment.getId())) {
                matchResult.add(new PaymentMatchingResult(
                        ReconciliationRecordType.INTERNAL_PAYMENT,
                        payment.getReference(),
                        payment.getId(),
                        false,
                        List.of("MISSING_PROVIDER_TRANSACTION")));

                unmatchedInternalCount++;
            }
        }

        // Return the summary
        return new PaymentMatchingSummary(
                providerTransactions.size(),
                internalPayment.size(),
                matchedCount,
                discepantCount,
                unmatchedPaystackCount,
                unmatchedInternalCount,
                matchResult);

    }
}
