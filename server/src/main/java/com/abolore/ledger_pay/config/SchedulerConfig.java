package com.abolore.ledger_pay.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.abolore.ledger_pay.reconciliation.entity.ReconciliationSchedulerProperties;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ReconciliationSchedulerProperties.class)
public class SchedulerConfig {

    @Bean
    public Clock reconciliationClock() {
        return Clock.systemUTC();
    }
}
