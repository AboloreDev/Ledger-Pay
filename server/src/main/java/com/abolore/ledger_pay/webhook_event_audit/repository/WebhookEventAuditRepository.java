package com.abolore.ledger_pay.webhook_event_audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abolore.ledger_pay.webhook_event_audit.entity.WebhookEventAudit;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WebhookEventAuditRepository extends JpaRepository<WebhookEventAudit, UUID> {

    Optional<WebhookEventAudit> findByProviderAndDeduplicationKey(String provider, String deduplicationKey);

    @Modifying
    @Query(value = """
            INSERT INTO webhook_event_audit (
                id,
                provider,
                deduplication_key,
                event_type,
                transaction_reference,
                payload,
                processing_status,
                attempts,
                received_at
            )
            VALUES (
                :id,
                :provider,
                :deduplicationKey,
                :eventType,
                :transactionReference,
                CAST(:payload AS jsonb),
                'RECEIVED',
                0,
                :receivedAt
            )
            ON CONFLICT (provider, deduplication_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("provider") String provider,
            @Param("deduplicationKey") String deduplicationKey,
            @Param("eventType") String eventType,
            @Param("transactionReference") String transactionReference,
            @Param("payload") String payload,
            @Param("receivedAt") Instant receivedAt);
}