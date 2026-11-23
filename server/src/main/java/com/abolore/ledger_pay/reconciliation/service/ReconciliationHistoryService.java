package com.abolore.ledger_pay.reconciliation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.reconciliation.dto.ReconciliationRunResponse;
import com.abolore.ledger_pay.reconciliation.mapper.ReconciliationRunMapper;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;

@Service
public class ReconciliationHistoryService {

    private final ReconciliationRunRepository reconciliationRunRepository;
    private final ReconciliationRunMapper reconciliationRunMapper;

    public ReconciliationHistoryService(ReconciliationRunRepository reconciliationRunRepository,
            ReconciliationRunMapper reconciliationRunMapper) {
        this.reconciliationRunRepository = reconciliationRunRepository;
        this.reconciliationRunMapper = reconciliationRunMapper;
    }

    public Page<ReconciliationRunResponse> getHistory(Pageable pageable) {
        return reconciliationRunRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(reconciliationRunMapper::toResponse);
    }
}
