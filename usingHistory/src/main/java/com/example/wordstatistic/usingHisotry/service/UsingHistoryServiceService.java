/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.usingHisotry.service;

import com.example.wordstatistic.usingHisotry.model.UsingHistoryRecord;
import com.example.wordstatistic.usingHisotry.repository.UsingHistoryRepository;
import com.example.wordstatistic.usingHisotry.util.UsingHistoryRecordIncorrectDataException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class UsingHistoryServiceService {
    private final UsingHistoryRepository usingHistoryRepository;

    @Autowired
    public UsingHistoryServiceService(final UsingHistoryRepository usingHistoryRepository) {
        this.usingHistoryRepository = usingHistoryRepository;
    }

    /**
     * add history message from other services to a using-analysis database.
     * @param serializedUsingHistoryRecord the text
     */
    @KafkaListener(topics = "usingHistory", groupId = "usingHistory-group")
    public void addNewUsingHistoryRecord(final @NotBlank String serializedUsingHistoryRecord)
        throws SQLException {
        final UsingHistoryRecord usingHistoryRecord;
        try {
            usingHistoryRecord = UsingHistoryRecord.fromJSON(serializedUsingHistoryRecord);
            if (usingHistoryRecord.getServiceName().equals("usingHistory")) {
                throw new UsingHistoryRecordIncorrectDataException("serviceName is usingHistory");
            }
            usingHistoryRepository.addRecord(usingHistoryRecord);
        } catch (Exception e) {
            usingHistoryRepository.addRecord(
                new UsingHistoryRecord(
                    "usingHistory",
                    "invalidMessage",
                    Map.of(
                        "exception_type", e.getClass().getName(),
                        "error_message", e.getMessage(),
                        "message", serializedUsingHistoryRecord
                    ),
                    Set.of(
                        "exception_type"
                    )
                )
            );
        }
    }
}
