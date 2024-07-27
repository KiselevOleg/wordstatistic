/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.dto.kafka.ChangeUsernameDTO;
import com.example.wordstatistic.localstatistic.dto.kafka.DeleteUserDTO;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class GettingMessagesFromOtherServicesService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final TopicRepository topicRepository;
    private final TextRepository textRepository;

    @Autowired
    public GettingMessagesFromOtherServicesService(
        final KafkaTemplate<String, String> kafkaTemplate,
        final TopicRepository topicRepository,
        final TextRepository textRepository
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicRepository = topicRepository;
        this.textRepository = textRepository;
    }

    /**
     * change a username to a new value.
     * @param text a ChangeUsernameDTO json
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    @KafkaListener(topics = "changeUsername", groupId = "localstatistic-group", concurrency = "2")
    public void changeUsername(final @NotBlank String text) throws KafkaDTOException, JsonProcessingException {
        final ChangeUsernameDTO dto = ChangeUsernameDTO.fromJSON(text);

        topicRepository.findAllByUserId(dto.userId()).forEach(e -> {
            e.setUserName(dto.newUsername());
            topicRepository.save(e);
        });
    }

    /**
     * delete all topics and texts that belong to a deleted user.
     * @param text a DeleteUserDTO json
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    @KafkaListener(topics = "deleteUser", groupId = "localstatistic-group", concurrency = "2")
    public void deleteUser(final @NotBlank String text) throws KafkaDTOException, JsonProcessingException {
        final DeleteUserDTO dto = DeleteUserDTO.fromJSON(text);

        topicRepository.findAllByUserId(dto.userId()).forEach(topic -> {
            textRepository.findAllByTopic(topic).forEach(textRepository::delete);
            topicRepository.delete(topic);
        });
    }
}
