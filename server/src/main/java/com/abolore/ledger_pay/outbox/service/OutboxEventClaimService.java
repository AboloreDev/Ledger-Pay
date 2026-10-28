package com.abolore.ledger_pay.outbox.service;

import com.abolore.ledger_pay.outbox.entity.OutboxEvent;
import com.abolore.ledger_pay.outbox.entity.OutboxEventStatus;
import com.abolore.ledger_pay.outbox.repository.OutboxEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OutboxEventClaimService {

    private static final long LEASE_SECONDS = 60;

    private final OutboxEventRepository outboxEventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public OutboxEventClaimService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public Optional<OutboxEvent> claimNextEvent() {
        Instant now = Instant.now();

        Object result = entityManager.createNativeQuery("""
                SELECT id
                FROM outbox_events
                WHERE
                    (status = 'PENDING' AND available_at <= :now)
                    OR
                    (status = 'PROCESSING' AND locked_until <= :now)
                ORDER BY available_at, created_at
                LIMIT 1
                FOR UPDATE SKIP LOCKED
                """)
                .setParameter("now", now)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (result == null) {
            return Optional.empty();
        }

        UUID eventId = (UUID) result;

        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Claimed outbox event was not found: "
                                + eventId));

        event.setStatus(OutboxEventStatus.PROCESSING);
        event.setAttempts(event.getAttempts() + 1);
        event.setLockedUntil(now.plusSeconds(LEASE_SECONDS));
        event.setUpdatedAt(now);
        event.setClaimToken(UUID.randomUUID());

        return Optional.of(outboxEventRepository.save(event));
    }
}
