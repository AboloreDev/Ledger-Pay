package com.abolore.ledger_pay.webhook.bachs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/webhooks/bachs")
public class BachsWebhookController {

    private final Logger log = LoggerFactory.getLogger(BachsWebhookController.class);

    private final BachsWebhookVerifier verifier;
    private final String webhookSecret;
    private final BachsWebhookEventService eventService;
    private final ObjectMapper objectMapper;
    private final BachsWebhookEventHandler eventHandler;

    public BachsWebhookController(BachsWebhookVerifier verifier,
            @Value("${bachs.webhook.secret:}") String webhookSecret,
            BachsWebhookEventService eventService,
            ObjectMapper objectMapper,
            BachsWebhookEventHandler eventHandler) {
        this.verifier = verifier;
        log.info("Bachs webhook secret configured: {}", !webhookSecret.isBlank());
        this.webhookSecret = webhookSecret;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.eventHandler = eventHandler;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<String> receiveWebhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Bachs-Signature-V2", required = false) String signatureHeader) {

        boolean valid = verifier.isValid(rawBody, signatureHeader, webhookSecret);

        if (!valid) {
            log.warn("Rejected Bachs webhook: invalid signature");
            return ResponseEntity.badRequest()
                    .body("Invalid webhook signature");
        }

        log.info("Bachs webhook signature verified");

        // parse and deduplicate the event, then process it.
        BachsWebhookEvent event = objectMapper.readValue(
                rawBody,
                BachsWebhookEvent.class);

        boolean claimed = eventService.claimEvent(
                event.id(),
                event.type());

        if (!claimed) {
            return ResponseEntity.ok("Duplicate event ignored");
        }

        eventHandler.handle(event);

        return ResponseEntity.ok("Event received");
    }
}
