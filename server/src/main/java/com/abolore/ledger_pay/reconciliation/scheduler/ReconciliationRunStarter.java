package com.abolore.ledger_pay.reconciliation.scheduler;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.abolore.ledger_pay.common.exceptions.DomainException;
import com.abolore.ledger_pay.common.exceptions.ErrorCode;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRun;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationRunStatus;
import com.abolore.ledger_pay.reconciliation.entity.ReconciliationTriggerType;
import com.abolore.ledger_pay.reconciliation.repository.ReconciliationRunRepository;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// This hsndles concurrency at db level and created the run
// Two runs cant overlap each other
@Service
public class ReconciliationRunStarter {

        private final JdbcTemplate jdbcTemplate;
        private final ReconciliationRunRepository reconciliationRunRepository;

        public ReconciliationRunStarter(JdbcTemplate jdbcTemplate,
                        ReconciliationRunRepository reconciliationRunRepository) {
                this.jdbcTemplate = jdbcTemplate;
                this.reconciliationRunRepository = reconciliationRunRepository;

        }

        @Transactional
        public ReconciliationRun startRun(String provider, Instant periodStart, Instant periodEnd,
                        ReconciliationTriggerType triggerType, String initiatedBy) {

                jdbcTemplate.query(
                                "SELECT pg_advisory_xact_lock(12345, hashtext(?))",
                                (ResultSetExtractor<Void>) rs -> null,
                                provider);

                Collection<ReconciliationRunStatus> statusToRun = List.of(ReconciliationRunStatus.PENDING,
                                ReconciliationRunStatus.RUNNING);

                long overlappingRuns = reconciliationRunRepository.countByProviderAndStatusInAndPeriodOverlap(provider,
                                statusToRun, periodStart, periodEnd);

                if (overlappingRuns > 0) {
                        throw DomainException.of(ErrorCode.RECONCILIATION_PROCESSING_CONFLICT,
                                        "A reconciliation run is already in progress for the specified period.");
                }

                ReconciliationRun run = ReconciliationRun.create(provider, periodStart, periodEnd, triggerType,
                                initiatedBy);

                run.start();

                return reconciliationRunRepository.saveAndFlush(run);
        }

}
