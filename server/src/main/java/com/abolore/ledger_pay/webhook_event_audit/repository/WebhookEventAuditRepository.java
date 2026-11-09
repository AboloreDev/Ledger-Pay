package com.abolore.ledger_pay.webhook_event_audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abolore.ledger_pay.webhook_event_audit.entity.WebhookEventAudit;

import java.util.Optional;
import java.util.UUID;

public interface WebhookEventAuditRepository extends JpaRepository<WebhookEventAudit, UUID> {

    Optional<WebhookEventAudit> findByProviderAndDeduplicationKey(String provider, String deduplicationKey);
}