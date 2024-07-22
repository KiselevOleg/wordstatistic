/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.client;

import com.example.wordstatistic.localstatistic.model.remote.usingHistory.UsingHistoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Set;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class UsingHistoryService {
    private static final String HISTORY_SERVICE_NAME = "usingHistory";
    private static final String THIS_SERVICE_NAME = "localStatistic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UsingHistoryService(
        final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(
        final String operationName,
        final Map<String, Object> parameters,
        final Set<String> primaryKey
    ) {
        kafkaTemplate.send(HISTORY_SERVICE_NAME, UsingHistoryRecord.withIngnoreExceptionsToJSON(
            new UsingHistoryRecord(
                THIS_SERVICE_NAME,
                operationName,
                parameters,
                primaryKey
            )
        ));
    }
}
