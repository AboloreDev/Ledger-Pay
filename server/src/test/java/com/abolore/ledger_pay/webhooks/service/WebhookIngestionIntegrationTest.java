package com.abolore.ledger_pay.webhooks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.abolore.ledger_pay.webhooks.repository.WebhookOutboxRepository;

@SpringBootTest
@ActiveProfiles("test")
class WebhookIngestionIntegrationTest {

  @Autowired
  private WebhookIngestionService webhookIngestionService;

  @Autowired
  private WebhookOutboxRepository webhookOutboxRepository;

  private final String payload = """
      {
        "event": "charge.success",
        "data": {
          "id": 123456789,
          "reference": "PAY-TEST-001",
          "status": "success",
          "amount": 500000,
          "currency": "NGN"
        }
      }
      """;

  @BeforeEach
  void cleanDatabase() {
    webhookOutboxRepository.deleteAll();
  }

  @Test
  void shouldInsertWebhookOnFirstDelivery() {

    webhookIngestionService.receiveWebhook(payload);

    assertEquals(1, webhookOutboxRepository.count());
  }

  @Test
  void shouldNotInsertDuplicateWebhook() {

    webhookIngestionService.receiveWebhook(payload);
    webhookIngestionService.receiveWebhook(payload);

    assertEquals(1, webhookOutboxRepository.count());
  }

  @Test
  void shouldInsertDifferentProviderTransaction() {

    webhookIngestionService.receiveWebhook(payload);

    String secondPayload = payload.replace(
        "123456789",
        "987654321");

    webhookIngestionService.receiveWebhook(secondPayload);

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