
package com.abolore.ledger_pay.bachs;

import org.junit.jupiter.api.Test;

import com.abolore.ledger_pay.webhook.bachs.BachsWebhookVerifier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BachsWebhookVerifierTest {

    private final BachsWebhookVerifier verifier = new BachsWebhookVerifier();

    private static final String TEST_SECRET = "local-test-secret";
    private static final String RAW_BODY = "{\"id\":\"evt_test_123\",\"type\":\"test.event\",\"data\":{}}";

    @Test
    void acceptsValidSignature() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = sign(timestamp, RAW_BODY, TEST_SECRET);

        String header = "t=" + timestamp + ",v1=" + signature;

        assertTrue(verifier.isValid(
                RAW_BODY.getBytes(StandardCharsets.UTF_8),
                header,
                TEST_SECRET));
    }

    @Test
    void rejectsInvalidSignature() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String header = "t=" + timestamp + ",v1=invalidsignature";

        assertFalse(verifier.isValid(
                RAW_BODY.getBytes(StandardCharsets.UTF_8),
                header,
                TEST_SECRET));
    }

    @Test
    void rejectsStaleTimestamp() throws Exception {
        String timestamp = String.valueOf(
                Instant.now().minusSeconds(600).getEpochSecond());

        String signature = sign(timestamp, RAW_BODY, TEST_SECRET);
        String header = "t=" + timestamp + ",v1=" + signature;

        assertFalse(verifier.isValid(
                RAW_BODY.getBytes(StandardCharsets.UTF_8),
                header,
                TEST_SECRET));
    }

    @Test
    void acceptsAnyMatchingSignatureDuringRotation() throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        String oldSecretSignature = sign(timestamp, RAW_BODY, "old-test-secret");
        String currentSecretSignature = sign(timestamp, RAW_BODY, TEST_SECRET);

        String header = "t=" + timestamp
                + ",v1=" + oldSecretSignature
                + ",v1=" + currentSecretSignature;

        assertTrue(verifier.isValid(
                RAW_BODY.getBytes(StandardCharsets.UTF_8),
                header,
                TEST_SECRET));
    }

    private String sign(
            String timestamp,
            String rawBody,
            String secret) throws Exception {

        String message = timestamp + "." + rawBody;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"));

        byte[] digest = mac.doFinal(
                message.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();

        for (byte b : digest) {
            hex.append(String.format("%02x", b & 0xff));
        }

        return hex.toString();
    }
}
