/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.*;
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
@RequestMapping("topicsAndTexts")
@Tag(
    name = "text controller",
    description = "a controller for add new texts"
)
@Validated
@SuppressWarnings("PMD.ClassFanOutComplexity")
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
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
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
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
        final @RequestParam @NotBlank String topicName,
        @Parameter(description = "a text name", example = "firstText")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
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

    /**
     * update an added topic for a current user.
     * @param topicDTO a topic updating dto
     * @return error if topic is not found or a new topic name is already used
     */
    @Operation(
        summary = "update an added topic",
        description = "update an added topic for a current user"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(EDIT_TEXT_PERMISSION_CHECK)
    @PutMapping(value = "updateTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateTopic(
        @Parameter(description = "a topic updating description")
        final @RequestBody @NotNull TopicUpdateDTO topicDTO
    ) {
        try {
            localTextService.updateTopic(
                CustomUserDetailsService.getId(),
                topicDTO.oldName(),
                topicDTO.newName()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * update an added text in a topic for a current user.
     * @param textDTO a text updating dto
     * @return error if topic or text is not found or a new text name is already used
     */
    @Operation(
        summary = "update an added text",
        description = "update an added text in a topic for a current user"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(EDIT_TEXT_PERMISSION_CHECK)
    @PutMapping(value = "updateText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateText(
        @Parameter(description = "a text updating description")
        final @RequestBody @NotNull TextUpdateDTO textDTO
    ) {
        try {
            localTextService.updateText(
                CustomUserDetailsService.getId(),
                textDTO.topic(),
                textDTO.oldName(),
                textDTO.newName(),
                textDTO.text()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * delete an added topic with all its texts for a current user.
     * @param topicDTO a topic entity dto
     * @return error if topic is not found
     */
    @Operation(
        summary = "delete an added topic",
        description = "delete an added topic with all its texts for a current user"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(EDIT_TEXT_PERMISSION_CHECK)
    @DeleteMapping(value = "deleteTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteTopic(
        @Parameter(description = "a topic dto")
        final @RequestBody @NotNull TopicDTO topicDTO
    ) {
        try {
            localTextService.deleteTopic(
                CustomUserDetailsService.getId(),
                topicDTO.name()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * delete an added text for a current user.
     * @param textDTO a topic deleting description
     * @return error if topic is not found
     */
    @Operation(
        summary = "delete a text",
        description = "delete an added text for a current user"
    )
    @SecurityRequirement(name = "JWT")
    @PreAuthorize(EDIT_TEXT_PERMISSION_CHECK)
    @DeleteMapping(value = "deleteText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteText(
        @Parameter(description = "a topic deleting description")
        final @RequestBody @NotNull TextDeleteDTO textDTO
    ) {
        try {
            localTextService.deleteText(
                CustomUserDetailsService.getId(),
                textDTO.topic(),
                textDTO.name()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }
}
