package com.abolore.ledger_pay.kafkaTopicConfig.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic webhookEventsTopic() {

        return TopicBuilder.name("webhook-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}