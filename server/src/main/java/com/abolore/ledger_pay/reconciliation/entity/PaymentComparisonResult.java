package com.abolore.ledger_pay.reconciliation.entity;

import java.util.List;

public record PaymentComparisonResult(String reference, boolean matched, List<String> discrepancies) {

}
