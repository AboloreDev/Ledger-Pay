package com.abolore.ledger_pay.webhooks.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abolore.ledger_pay.webhooks.entity.WebhookOutbox;

public interface WebhookOutboxRepository extends JpaRepository<WebhookOutbox, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO webhook_outbox (
                id,
                provider,
                event_type,
                deduplication_key,
                transaction_reference,
                payload
            )
            VALUES (
                :id,
                :provider,
                :eventType,
                :deduplicationKey,
                :transactionReference,
                CAST(:payload AS jsonb)
            )
            ON CONFLICT (provider, deduplication_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("provider") String provider,
            @Param("eventType") String eventType,
            @Param("deduplicationKey") String deduplicationKey,
            @Param("transactionReference") String transactionReference,
            @Param("payload") String payload);

    @Query(value = """
            SELECT *
            FROM webhook_outbox
            WHERE
                (
                    status = 'PENDING'
                    AND next_attempt_at <= :now
                )
                OR
                (
                    status = 'PROCESSING'
                    AND (lease_until IS NULL OR lease_until <= :now)
                )
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<WebhookOutbox> findEventsToClaim(@Param("now") Instant now, @Param("batchSize") int batchSize);
}
