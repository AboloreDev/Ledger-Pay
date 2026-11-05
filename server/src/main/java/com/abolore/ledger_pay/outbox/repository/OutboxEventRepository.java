
package com.abolore.ledger_pay.outbox.repository;

import com.abolore.ledger_pay.outbox.entity.OutboxEvent;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    boolean existsByAggregateIdAndEventType(UUID aggregateId, String eventType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEvent e WHERE e.id = :id")
    Optional<OutboxEvent> findByIdForUpdate(@Param("id") UUID id);

    @Query(value = """
             SELECT id
                FROM outbox_events
                WHERE
                    (status = 'PENDING' AND available_at <= :now)
                    OR
                    (status = 'PROCESSING' AND locked_until <= :now)
                ORDER BY available_at, created_at
                LIMIT 1
                FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<UUID> findNextEventIdToClaim(@Param("now") java.time.Instant now);
}
