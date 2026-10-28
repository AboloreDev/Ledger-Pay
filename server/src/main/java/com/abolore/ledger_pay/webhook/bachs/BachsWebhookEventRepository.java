package com.abolore.ledger_pay.webhook.bachs;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BachsWebhookEventRepository extends JpaRepository<BachsWebhookEventEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO bachs_webhook_events (
                id,
                provider_event_id,
                event_type,
                received_at,
                status
            )
            VALUES (
                :id,
                :eventId,
                :eventType,
                CURRENT_TIMESTAMP,
                'RECEIVED'
            )
            ON CONFLICT (provider_event_id) DO NOTHING
            """, nativeQuery = true)

    int claimEvent(
            @Param("id") UUID id,
            @Param("eventId") String eventId,
            @Param("eventType") String eventType);
}