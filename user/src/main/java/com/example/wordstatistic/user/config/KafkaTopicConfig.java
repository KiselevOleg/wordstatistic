/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kiselev Oleg
 */
@Configuration
public class KafkaTopicConfig {
    private static final String BOOT_STRAP_ADDRESS = "kafka:9092";

    @Bean
    public KafkaAdmin kafkaAdmin() {
        final Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_ADDRESS);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic usingHistory() {
        return new NewTopic("usingHistory", 1, (short) 1);
    }
    @Bean
    public NewTopic changeUsername() {
        return new NewTopic("changeUsername", 1, (short) 1);
    }
    @Bean
    public NewTopic deleteUser() {
        return new NewTopic("deleteUser", 1, (short) 1);
    }
}
