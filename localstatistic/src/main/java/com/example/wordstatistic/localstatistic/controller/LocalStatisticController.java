/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.localstatistic.service.LocalStatisticService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
    name = "local statistic controller",
    description = "a controller for getting statistic for local user's texts"
)
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
    @Operation(
        summary = "get most popular user words",
        description = "get most popular words for all user's texts"
    )
    @GetMapping(value = "/getMostPopularWordsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForUser(
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @Min(1) Integer limit,
        @Parameter(description = "token")
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
    @Operation(
        summary = "get most popular topic words",
        description = "get most popular words for all topic's texts"
    )
    @GetMapping(value = "/getMostPopularWordsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForTopic(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        final @RequestParam @NotBlank String topicName,
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @Min(1) Integer limit,
        @Parameter(description = "token")
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
    @Operation(
        summary = "get most popular text words",
        description = "get most popular words for selected text"
    )
    @GetMapping(value = "/getMostPopularWordsForText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForText(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        final @RequestParam @NotBlank String topicName,
        @Parameter(description = "a text name", example = "firstText")
        final @RequestParam @NotBlank String textName,
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @Min(1) Integer limit,
        @Parameter(description = "token")
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
