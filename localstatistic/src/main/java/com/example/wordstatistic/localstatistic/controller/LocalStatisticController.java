/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.WordDTO;
import com.example.wordstatistic.localstatistic.security.CustomUserDetailsService;
import com.example.wordstatistic.localstatistic.service.LocalStatisticService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Validated
@CrossOrigin(maxAge = 60L, origins = {"http://localhost", "http://localhost:80", "http://localhost:3000"})
@SuppressWarnings("PMD.ReturnCount")
public class LocalStatisticController {
    public static final String VIEW_TEXT_PERMISSION = "viewText";
    public static final String VIEW_TEXT_PERMISSION_CHECK =
        "hasAuthority('" + VIEW_TEXT_PERMISSION + "')";

    private LocalStatisticService localStatisticService;

    @Autowired
    public LocalStatisticController(
        final LocalStatisticService localStatisticService
    ) {
        this.localStatisticService = localStatisticService;
    }

    /**
     * get a list of most used words in texts for a current user.
     * @param limit a size list limit
     * @return the list
     */
    @Operation(
        summary = "get most popular user words",
        description = "get most popular words for all user's texts"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(VIEW_TEXT_PERMISSION_CHECK)
    @GetMapping(value = "/getMostPopularWordsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WordDTO>> getMostPopularWordsForUser(
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @Min(1) Integer limit
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            localStatisticService.getMostPopularWordsForUser(CustomUserDetailsService.getId(), limit)
        );
    }

    /**
     * get a list of most used words in texts for a selected topic.
     * @param topicName a topic's name
     * @param limit a size list limit
     * @return the list
     */
    @Operation(
        summary = "get most popular topic words",
        description = "get most popular words for all topic's texts"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(VIEW_TEXT_PERMISSION_CHECK)
    @GetMapping(value = "/getMostPopularWordsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForTopic(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
        final @RequestParam @NotBlank String topicName,
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @Min(1) Integer limit
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localStatisticService.getMostPopularWordsForTopic(CustomUserDetailsService.getId(), topicName, limit)
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
     * @return the list
     */
    @Operation(
        summary = "get most popular text words",
        description = "get most popular words for selected text"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(VIEW_TEXT_PERMISSION_CHECK)
    @GetMapping(value = "/getMostPopularWordsForText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWordsForText(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
        final @RequestParam @NotBlank String topicName,
        @Parameter(description = "a text name", example = "firstText")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
        final @RequestParam @NotBlank String textName,
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @Min(1) Integer limit
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localStatisticService.getMostPopularWordsForText(
                    CustomUserDetailsService.getId(), topicName, textName, limit
                )
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
}
