/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.controller;

import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.globalstatistic.service.WordService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Kiselev Oleg
 */
@RestController
@RequestMapping("globalStatistic")
public class GlobalStatistic {
    private final WordService wordService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    GlobalStatistic(
        final WordService wordService,
        final JwtTokenProvider jwtTokenProvider
    ) {
        this.wordService = wordService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * get a list of most popular words in all added texts.
     * @param limit a size of the list
     * @return the list
     */
    @GetMapping(value = "/getMostPopularWords", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMostPopularWords(
        final @RequestParam @NotNull Integer limit
    ) {
        if (limit >= 1) {
            return ResponseEntity.status(HttpStatus.OK).body(
                wordService.getMostPopularWords(limit).stream().map(Word::toDTO).toList()
            );
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                "limit must be a positive integer"
            );
        }
    }

    /**
     * add a new text (changes a most popular word statistic).
     * @param text the text
     * @param token a user's jwt token
     * @return HttpStatus
     */
    @PostMapping(value = "/addText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addText(
        final @RequestBody @NotBlank String text,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains("addTextToGlobal")) {
            return new ResponseEntity<>("invalid token", HttpStatus.FORBIDDEN);
        }
        wordService.addNewText(text);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
