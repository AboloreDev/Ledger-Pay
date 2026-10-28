package com.abolore.ledger_pay.webhook.bachs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class BachsWebhookVerifier {

    private static final long TOLERANCE_SECONDS = 300;

    public boolean isValid(byte[] rawBody, String signatureHeader, String secret) {

        if (rawBody == null || signatureHeader == null || secret == null || secret.isBlank()) {
            return false;
        }

        try {
            String timestampText = null;
            List<String> signatures = new ArrayList<>();

            for (String part : signatureHeader.split(",")) {
                String[] pair = part.trim().split("=", 2);

                if (pair.length != 2) {
                    continue;
                }

                String value = pair[1].trim();
                String key = pair[0].trim();

                if (key.equals("t")) {
                    timestampText = value;
                } else if (key.equals("v1")) {
                    signatures.add(value);
                }
            }

            if (timestampText == null || signatures.isEmpty()) {
                return false;
            }

            long timestamp = Long.parseLong(timestampText);
            long now = Instant.now().getEpochSecond();

            if (Math.abs(now - timestamp) > TOLERANCE_SECONDS) {
                return false;
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));

            mac.update(
                    (timestampText + ".").getBytes(StandardCharsets.UTF_8));

            byte[] digest = mac.doFinal(rawBody);
            String expected = toHex(digest);

            for (String signature : signatures) {
                if (MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.US_ASCII),
                        signature.getBytes(StandardCharsets.US_ASCII))) {
                    return true;
                }
            }

            return false;

        } catch (Exception exception) {
            return false;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for (byte value : bytes) {
            result.append(String.format("%02x", value & 0xff));
        }

        return result.toString();
    }
}
