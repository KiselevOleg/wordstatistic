/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.service.LocalTextService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Kiselev Oleg
 */
@RestController
@RequestMapping("topicsAndTexts")
public class TextController {
    private final LocalTextService localTextService;

    @Autowired
    public TextController(final LocalTextService localTextService) {
        this.localTextService = localTextService;
    }

    /**
     * get all topics that belong to a selected user.
     * @param userId a user's id
     * @return a list of the topic names
     */
    @GetMapping(value = "/getAllTopicsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTopicsForUser(final @RequestParam @NotNull UUID userId) {
        return ResponseEntity.status(HttpStatus.OK).body(
            localTextService.getAllTopicForUser(userId).stream().map(Topic::toDTO).toList()
        );
    }

    /**
     * get all texts that belong to a selected topic.
     * @param userId a user's id
     * @param topicName a topic's name
     * @return a list of the texts names
     */
    @GetMapping(value = "/getAllTextsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTextsForTopic(
        final @RequestParam @NotNull UUID userId,
        final @RequestParam @NotBlank String topicName) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localTextService.getAllTextsForSelectedTopic(userId, topicName)
                    .stream().map(Text::toListDTO).toList()
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * get a content of a selected text.
     * @param userId  a user's id
     * @param topicName a topic's name
     * @param textName a text's name
     * @return a string with the text
     */
    @GetMapping(value = "/getTextContent", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTextContent(
        final @RequestParam @NotNull UUID userId,
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @NotBlank String textName) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localTextService.getTextForSelectedTextName(userId, topicName, textName)
                    .map(Text::toEntityDTO)
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * add a new topic for a selected user.
     * @param userId a user's id
     * @param username a user's name
     * @param topicDTO a topic dto
     * @return error if topic already exists
     */
    @PostMapping(value = "/addNewTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNewTopic(
        final @RequestParam @NotNull UUID userId,
        final @RequestParam @NotBlank String username,
        final @RequestBody @NotNull TopicDTO topicDTO) {
        try {
            localTextService.addTopic(userId, username, topicDTO.name());
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * add a new text for a selected topic.
     * @param userId a user's id
     * @param textDTO a text  entity dto
     * @return error if topic does not exist or test already exists
     */
    @PostMapping(value = "addNewText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNewText(
        final @RequestParam @NotNull UUID userId,
        final @RequestBody @NotNull TextEntityDTO textDTO) {
        try {
            localTextService.addText(userId, textDTO.topic(), textDTO.name(), textDTO.text());
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }
}
