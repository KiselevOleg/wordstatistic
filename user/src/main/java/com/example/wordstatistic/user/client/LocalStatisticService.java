/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.client;

import com.example.wordstatistic.user.dto.kafka.ChangeUsernameDTO;
import com.example.wordstatistic.user.dto.kafka.DeleteUserDTO;
import com.example.wordstatistic.user.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class LocalStatisticService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public LocalStatisticService(
        final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * send a message about deleting a user.
     * @param dto a dto object
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    public void send(final DeleteUserDTO dto) {
        try {
            kafkaTemplate.send("deleteUser", DeleteUserDTO.toJSON(dto));
        } catch (KafkaDTOException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * send a message about updating a username.
     * @param dto a dto object
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    public void send(final ChangeUsernameDTO dto) {
        try {
            kafkaTemplate.send("changeUsername", ChangeUsernameDTO.toJSON(dto));
        } catch (KafkaDTOException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
