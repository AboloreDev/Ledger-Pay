package com.abolore.ledger_pay.webhooks.security;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class RetryBackoffPolicy {

    private static final Duration INITIAL_DELAY = Duration.ofSeconds(5);
    private static final Duration MAX_DELAY = Duration.ofMinutes(5);

    public Duration calculateDelay(int attemptCount) {

        if (attemptCount < 1) {
            throw new IllegalArgumentException("Attempt count must be at least 1");
        }

        long initialMillis = INITIAL_DELAY.toMillis();
        long maximumMillis = MAX_DELAY.toMillis();

        int exponent = Math.min(attemptCount - 1, 30);

        long exponentialDelay = initialMillis * (1L << exponent);

        long cappedDelay = Math.min(exponentialDelay, maximumMillis);

        long minimumJitter = cappedDelay / 2;

        long jitteredDelay = ThreadLocalRandom.current().nextLong(minimumJitter, cappedDelay + 1);

        return Duration.ofMillis(jitteredDelay);
    }
}
