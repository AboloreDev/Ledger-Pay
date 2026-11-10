package com.abolore.ledger_pay.webhooks.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abolore.ledger_pay.webhooks.security.PaystackWebhookSignatureValidator;
import com.abolore.ledger_pay.webhooks.service.WebhookIngestionService;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final PaystackWebhookSignatureValidator signatureValidator;
    private final WebhookIngestionService webhookIngestionService;

    public WebhookController(PaystackWebhookSignatureValidator signatureValidator,
            WebhookIngestionService webhookIngestionService) {
        this.signatureValidator = signatureValidator;
        this.webhookIngestionService = webhookIngestionService;
    }

    @PostMapping("/paystack")
    public ResponseEntity<Void> receivePaystackWebhook(@RequestBody String rawPayload,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {

        boolean valid = signatureValidator.isValid(rawPayload, signature);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        webhookIngestionService.receiveWebhook(rawPayload);

        return ResponseEntity.ok().build();
    }

}
