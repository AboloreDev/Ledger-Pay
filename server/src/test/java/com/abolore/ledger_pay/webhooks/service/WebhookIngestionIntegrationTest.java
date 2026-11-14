package com.abolore.ledger_pay.webhooks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.abolore.ledger_pay.webhook_event_audit.repository.WebhookEventAuditRepository;
import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;

@SpringBootTest
@ActiveProfiles("test")
class WebhookIngestionIntegrationTest {

  @Autowired
  private WebhookIngestionService webhookIngestionService;

  @Autowired
  private WebhookEventAuditRepository webhookEventAuditRepository;

  @Autowired
  private WebhookOutboxRepository webhookOutboxRepository;

  private String payload(long providerId, String reference) {
    return """
        {
          "event": "charge.success",
          "data": {
            "id": %d,
            "reference": "%s",
            "status": "success",
            "amount": 500000,
            "currency": "NGN"
          }
        }
        """.formatted(providerId, reference);
  }

  @BeforeEach
  void cleanDatabase() {
    webhookOutboxRepository.deleteAll();
    webhookEventAuditRepository.deleteAll();
  }

  @Test
  void shouldInsertWebhookOnFirstDelivery() {

    String payload = payload(System.nanoTime(), "PAY-" + UUID.randomUUID());

    webhookIngestionService.receiveWebhook(payload);

    assertEquals(1, webhookOutboxRepository.count());
  }

  @Test
  void shouldNotInsertDuplicateWebhook() {

    String payload = payload(System.nanoTime(), "PAY-" + UUID.randomUUID());

    webhookIngestionService.receiveWebhook(payload);
    webhookIngestionService.receiveWebhook(payload);

    assertEquals(1, webhookOutboxRepository.count());
  }

  @Test
  void shouldInsertDifferentProviderTransaction() {

    webhookIngestionService.receiveWebhook(payload(System.nanoTime(), "PAY-" + UUID.randomUUID()));
    webhookIngestionService.receiveWebhook(payload(System.nanoTime(), "PAY-" + UUID.randomUUID()));

    assertEquals(2, webhookOutboxRepository.count());
  }

  @Test
  void shouldIgnoreUnsupportedEvent() {

    String unsupportedPayload = """
        {
          "event": "transfer.success",
          "data": {}
        }
        """;

    webhookIngestionService.receiveWebhook(unsupportedPayload);

    assertEquals(0, webhookOutboxRepository.count());
  }
}