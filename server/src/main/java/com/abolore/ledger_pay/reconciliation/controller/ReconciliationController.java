package com.abolore.ledger_pay.reconciliation.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.reconciliation.dto.ReconciliationRequest;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.entity.TransactionReconciliationResult;
import com.abolore.ledger_pay.reconciliation.service.TransactionReconciliationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reconciliations")
public class ReconciliationController {

    private final TransactionReconciliationService reconciliationService;

    public ReconciliationController(
            TransactionReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping
    public ResponseEntity<TransactionReconciliationResult> reconcile(
            @Valid @RequestBody ReconciliationRequest request,
            Principal principal) {

        TransactionReconciliationResult result = reconciliationService.reconcile(
                request.periodStart(),
                request.periodEnd(),
                ReconciliationTriggerType.MANUAL,
                principal.getName());

        return ResponseEntity.ok(result);
    }
}