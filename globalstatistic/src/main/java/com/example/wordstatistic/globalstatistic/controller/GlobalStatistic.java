/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.controller;

import com.example.wordstatistic.globalstatistic.dto.WordDTO;
import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@CrossOrigin(maxAge = 60L, origins = {"http://localhost", "http://localhost:80", "http://localhost:3000"})
@RequestMapping("globalStatistic")
@Tag(
    name = "global statistic controller",
    description = "a controller for getting statistic for all words have been ever added"
)
@Validated
public class GlobalStatistic {
    private final WordService wordService;

    @Autowired
    GlobalStatistic(
        final WordService wordService
    ) {
        this.wordService = wordService;
    }

    /**
     * get a list of most popular words in all added texts.
     * @param limit a size of the list
     * @return the list
     */
    @Operation(
        summary = "get most popular words",
        description = "get a list of most popular words in all added texts"
    )
    @GetMapping(value = "/getMostPopularWords", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WordDTO>> getMostPopularWords(
        @Parameter(description = "count of words", example = "3")
        final @RequestParam @NotNull @Min(1) Integer limit
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            wordService.getMostPopularWords(limit).stream().map(Word::toDTO).toList()
        );
    }

    /**
     * add a new text (changes a most popular word statistic).
     * @param text the text
     * @return HttpStatus
     */
    @Operation(
        summary = "a new text",
        description = "add a new text (changes a most popular word statistic)"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize("hasAuthority('addTextToGlobal')")
    @PostMapping(value = "/addText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addText(
        final @RequestBody @NotBlank @Parameter(description = "an added text") String text
    ) {
        wordService.addNewText(text);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
