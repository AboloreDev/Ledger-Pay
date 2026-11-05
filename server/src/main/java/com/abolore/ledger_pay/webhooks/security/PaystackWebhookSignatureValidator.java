package com.abolore.ledger_pay.webhooks.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class PaystackWebhookSignatureValidator {

    private static final String HMAC_SHA512 = "HmacSHA512";

    private final String secretKey;

    public PaystackWebhookSignatureValidator(@Value("${paystack.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isValid(String rawPayload, String signature) {

        if (rawPayload == null || signature == null || signature.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);

            mac.init(secretKeySpec);

            byte[] calculatedSignature = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));

            byte[] receivedSignature = HexFormat.of().parseHex(signature);

            return MessageDigest.isEqual(calculatedSignature, receivedSignature);

        } catch (Exception exception) {
            return false;
        }
    }
}