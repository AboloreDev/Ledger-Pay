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

        return outboxEventRepository.findNextEventIdToClaim(now)
                .map(eventId -> {
                    OutboxEvent event = outboxEventRepository.findById(eventId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Claimed outbox event was not found: " + eventId));

                    event.setStatus(OutboxEventStatus.PROCESSING);
                    event.setAttempts(event.getAttempts() + 1);
                    event.setLockedUntil(now.plusSeconds(LEASE_SECONDS));
                    event.setUpdatedAt(now);
                    event.setClaimToken(UUID.randomUUID());

                    return event;
                });
    }
}
