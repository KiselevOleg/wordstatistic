/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.service.LocalStatisticService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Kiselev Oleg
 */
@RestController
@RequestMapping("localStatistic")
public class LocalStatisticController {
    private LocalStatisticService localStatisticService;

    @Autowired
    public LocalStatisticController(final LocalStatisticService localStatisticService) {
        this.localStatisticService = localStatisticService;
    }

    /**
     * get a list of most used words in texts for a current user.
     * @param userId a user's id
     * @param limit a size list limit
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWordsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForUser(
        final @RequestParam @NotNull UUID userId,
        final @RequestParam @Min(1) Integer limit
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            localStatisticService.getMostPopularWordsForUser(userId, limit)
        );
    }

    /**
     * get a list of most used words in texts for a selected topic.
     * @param userId a user's id
     * @param topicName a topic's name
     * @param limit a size list limit
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWordsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForTopic(
        final @RequestParam @NotNull UUID userId,
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @Min(1) Integer limit
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localStatisticService.getMostPopularWordsForTopic(userId, topicName, limit)
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
    /**
     * get a list of most used words in texts for a selected text.
     * @param userId a user's id
     * @param topicName a topic's name
     * @param textName a text's name
     * @param limit a size list limit
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWordsForText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForText(
        final @RequestParam @NotNull UUID userId,
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @NotBlank String textName,
        final @RequestParam @Min(1) Integer limit
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localStatisticService.getMostPopularWordsForText(userId, topicName, textName, limit)
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
}
