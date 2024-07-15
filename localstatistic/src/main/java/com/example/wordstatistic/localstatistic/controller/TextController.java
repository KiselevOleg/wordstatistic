/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.security.CustomUserDetailsService;
import com.example.wordstatistic.localstatistic.service.LocalTextService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("topicsAndTexts")
@Tag(
    name = "text controller",
    description = "a controller for add new texts"
)
@Validated
@SuppressWarnings("PMD.ReturnCount")
public class TextController {
    public static final String VIEW_TEXT_PERMISSION = "viewText";
    public static final String VIEW_TEXT_PERMISSION_CHECK =
        "hasAuthority('" + VIEW_TEXT_PERMISSION + "')";
    public static final String EDIT_TEXT_PERMISSION = "editText";
    public static final String EDIT_TEXT_PERMISSION_CHECK =
        "hasAuthority('" + EDIT_TEXT_PERMISSION + "')";

    private final LocalTextService localTextService;

    @Autowired
    public TextController(final LocalTextService localTextService) {
        this.localTextService = localTextService;
    }

    /**
     * get all topics that belong to a selected user.
     * @return a list of the topic names
     */
    @Operation(
        summary = "get all topics",
        description = "get a list of all names of user's topics"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(VIEW_TEXT_PERMISSION_CHECK)
    @GetMapping(value = "/getAllTopicsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TopicDTO>> getAllTopicsForUser() {
        return ResponseEntity.status(HttpStatus.OK).body(
            localTextService.getAllTopicForUser(CustomUserDetailsService.getId()).stream().map(Topic::toDTO).toList()
        );
    }

    /**
     * get all texts that belong to a selected topic.
     * @param topicName a topic's name
     * @return a list of the texts names
     */
    @Operation(
        summary = "get all topic texts",
        description = "get a list of all names of texts in a selected topic"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(VIEW_TEXT_PERMISSION_CHECK)
    @GetMapping(value = "/getAllTextsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTextsForTopic(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        final @RequestParam @NotBlank String topicName
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localTextService.getAllTextsForSelectedTopic(CustomUserDetailsService.getId(), topicName)
                    .stream().map(Text::toListDTO).toList()
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * get a content of a selected text.
     * @param topicName a topic's name
     * @param textName a text's name
     * @return a string with the text
     */
    @Operation(
        summary = "get a text",
        description = "get a content of a selected text"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(VIEW_TEXT_PERMISSION_CHECK)
    @GetMapping(value = "/getTextContent", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTextContent(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        final @RequestParam @NotBlank String topicName,
        @Parameter(description = "a text name", example = "firstText")
        final @RequestParam @NotBlank String textName
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localTextService.getTextForSelectedTextName(CustomUserDetailsService.getId(), topicName, textName)
                    .map(Text::toEntityDTO)
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * add a new topic for a selected user.
     * @param topicDTO a topic dto
     * @return error if topic already exists
     */
    @Operation(
        summary = "add a new topic",
        description = "add a new topic for a current user"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(EDIT_TEXT_PERMISSION_CHECK)
    @PostMapping(value = "/addNewTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNewTopic(
        @Parameter(description = "a topic name", example = "ownTestTopic")
        final @RequestBody @NotNull TopicDTO topicDTO
    ) {
        try {
            localTextService.addTopic(
                CustomUserDetailsService.getId(), CustomUserDetailsService.getUsername(), topicDTO.name()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * add a new text for a selected topic.
     * @param textDTO a text  entity dto
     * @return error if topic does not exist or test already exists
     */
    @Operation(
        summary = "add a new text",
        description = "add a new text in a topic for a current user"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(EDIT_TEXT_PERMISSION_CHECK)
    @PostMapping(value = "addNewText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNewText(
        @Parameter(description = "a text description")
        final @RequestBody @NotNull TextEntityDTO textDTO
    ) {
        try {
            localTextService.addText(
                CustomUserDetailsService.getId(),
                textDTO.topic(),
                textDTO.name(),
                textDTO.text()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }
}
