/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.localstatistic.service.LocalStatisticService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Kiselev Oleg
 */
@RestController
@RequestMapping("localStatistic")
@SuppressWarnings("PMD.ReturnCount")
public class LocalStatisticController {
    private static final String VIEW_TEXT_PERMISSION = "viewText";
    private static final String INVALID_TOKEN_ERROR_MESSAGE = "invalid token";

    private LocalStatisticService localStatisticService;
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public LocalStatisticController(
        final LocalStatisticService localStatisticService,
        final JwtTokenProvider jwtTokenProvider
    ) {
        this.localStatisticService = localStatisticService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * get a list of most used words in texts for a current user.
     * @param limit a size list limit
     * @param token a user's jwt token
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWordsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForUser(
        final @RequestParam @Min(1) Integer limit,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(VIEW_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.status(HttpStatus.OK).body(
            localStatisticService.getMostPopularWordsForUser(jwtTokenProvider.getId(token), limit)
        );
    }

    /**
     * get a list of most used words in texts for a selected topic.
     * @param topicName a topic's name
     * @param limit a size list limit
     * @param token a user's jwt token
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWordsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForTopic(
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @Min(1) Integer limit,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(VIEW_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localStatisticService.getMostPopularWordsForTopic(jwtTokenProvider.getId(token), topicName, limit)
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
    /**
     * get a list of most used words in texts for a selected text.
     * @param topicName a topic's name
     * @param textName a text's name
     * @param limit a size list limit
     * @param token a user's jwt token
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWordsForText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForText(
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @NotBlank String textName,
        final @RequestParam @Min(1) Integer limit,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(VIEW_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localStatisticService.getMostPopularWordsForText(
                    jwtTokenProvider.getId(token), topicName, textName, limit
                )
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
}
