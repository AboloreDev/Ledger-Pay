package com.abolore.ledger_pay.reconciliation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.auth.entity.Admin;
import com.abolore.ledger_pay.reconciliation.dto.ReconciliationRequest;
import com.abolore.ledger_pay.reconciliation.dto.ReconciliationRunResponse;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.entity.TransactionReconciliationResult;
import com.abolore.ledger_pay.reconciliation.service.ReconciliationHistoryService;
import com.abolore.ledger_pay.reconciliation.service.TransactionReconciliationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reconciliations")
public class ReconciliationController {

    private final TransactionReconciliationService reconciliationService;
    private final ReconciliationHistoryService reconciliationHistoryService;

    public ReconciliationController(
            TransactionReconciliationService reconciliationService,
            ReconciliationHistoryService reconciliationHistoryService) {
        this.reconciliationService = reconciliationService;
        this.reconciliationHistoryService = reconciliationHistoryService;

    }

    @PostMapping
    public ResponseEntity<TransactionReconciliationResult> reconcile(
            @Valid @RequestBody ReconciliationRequest request, Authentication authentication) {

        TransactionReconciliationResult response = reconciliationService.reconcile(
                request.periodStart(),
                request.periodEnd(),
                ReconciliationTriggerType.MANUAL,
                authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReconciliationRunResponse>> getReconciliationHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReconciliationRunResponse> history = reconciliationHistoryService.getHistory(pageable);
        return ResponseEntity.ok(history);
    }
}