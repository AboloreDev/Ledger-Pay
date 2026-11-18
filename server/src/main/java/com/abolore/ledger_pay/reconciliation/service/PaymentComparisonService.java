package com.abolore.ledger_pay.reconciliation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.payment.entity.Payment;
import com.abolore.ledger_pay.payment.entity.PaymentStatus;
import com.abolore.ledger_pay.reconciliation.entity.PaymentComparisonResult;
import com.abolore.ledger_pay.reconciliation.entity.PaystackTransactionResponse;

@Service
public class PaymentComparisonService {

    public PaymentComparisonResult compare(PaystackTransactionResponse transactionResponse, Payment payment) {

        List<String> discrepancies = new ArrayList<>();

        // Comapre References
        if (!Objects.equals(transactionResponse.reference(), payment.getReference())) {
            discrepancies.add(String.format(
                    "REFERENCE MISMATCH: Paystack reference '%s' does not match internal payment reference '%s'",
                    transactionResponse.reference(), payment.getReference()));
        }

        // Compare amount
        BigDecimal paystackAmount = BigDecimal.valueOf(transactionResponse.amount()).movePointLeft(2);

        if (paystackAmount.compareTo(payment.getAmount()) != 0) {
            discrepancies.add(String.format(
                    "AMOUNT MISMATCH: Paystack amount '%s' does not match internal payment amount '%s'",
                    paystackAmount, payment.getAmount()));
        }

        // Compare currency
        if (!Objects.equals(transactionResponse.currency(), payment.getCurrency())) {
            discrepancies.add(String.format(
                    "CURRENCY MISMATCH: Paystack currency '%s' does not match internal payment currency '%s'",
                    transactionResponse.currency(), payment.getCurrency()));
        }

        // Compare payment statuses
        if ("success".equalsIgnoreCase(transactionResponse.status())
                && payment.getStatus() != PaymentStatus.SUCCEEDED) {
            discrepancies.add(String.format(
                    "STATUS MISMATCH: Paystack status '%s' does not match internal payment status '%s'",
                    transactionResponse.status(), payment.getStatus()));
        } else if ("failed".equalsIgnoreCase(transactionResponse.status())
                && payment.getStatus() != PaymentStatus.FAILED) {
            discrepancies.add(String.format(
                    "STATUS MISMATCH: Paystack status '%s' does not match internal payment status '%s'",
                    transactionResponse.status(), payment.getStatus()));
        }

        // Compare providerCheckoutID
        if (payment.getProviderCheckoutId() != null
                && Objects.equals(String.valueOf(transactionResponse.id()), payment.getProviderCheckoutId())) {
            discrepancies.add(String.format(
                    "CHECKOUT ID MISMATCH: Paystack checkout ID '%s' does not match internal payment checkout ID '%s'",
                    transactionResponse.id(), payment.getProviderCheckoutId()));
        }

        return new PaymentComparisonResult(transactionResponse.reference(), discrepancies.isEmpty(), discrepancies);
    }
}
