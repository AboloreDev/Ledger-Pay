package com.abolore.ledger_pay.reconciliation.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reconciliation")
public record ReconciliationSchedulerProperties(Scheduler scheduler) {

    public record Scheduler(boolean isEnabled, String cron, int lookBackDays) {
    }
}
