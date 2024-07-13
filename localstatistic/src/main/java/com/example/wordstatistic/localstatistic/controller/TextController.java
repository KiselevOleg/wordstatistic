/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.localstatistic.service.LocalTextService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
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
@RequestMapping("topicsAndTexts")
@SuppressWarnings("PMD.ReturnCount")
public class TextController {
    private static final String VIEW_TEXT_PERMISSION = "viewText";
    private static final String EDIT_TEXT_PERMISSION = "editText";
    private static final String INVALID_TOKEN_ERROR_MESSAGE = "invalid token";

    private final LocalTextService localTextService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public TextController(final LocalTextService localTextService, final JwtTokenProvider jwtTokenProvider) {
        this.localTextService = localTextService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * get all topics that belong to a selected user.
     * @param token a user's jwt token
     * @return a list of the topic names
     */
    @GetMapping(value = "/getAllTopicsForUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTopicsForUser(final @RequestParam @NotBlank String token) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(VIEW_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.status(HttpStatus.OK).body(
            localTextService.getAllTopicForUser(jwtTokenProvider.getId(token)).stream().map(Topic::toDTO).toList()
        );
    }

    /**
     * get all texts that belong to a selected topic.
     * @param topicName a topic's name
     * @param token a user's jwt token
     * @return a list of the texts names
     */
    @GetMapping(value = "/getAllTextsForTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllTextsForTopic(
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(VIEW_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localTextService.getAllTextsForSelectedTopic(jwtTokenProvider.getId(token), topicName)
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
     * @param token a user's jwt token
     * @return a string with the text
     */
    @GetMapping(value = "/getTextContent", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTextContent(
        final @RequestParam @NotBlank String topicName,
        final @RequestParam @NotBlank String textName,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(VIEW_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                localTextService.getTextForSelectedTextName(jwtTokenProvider.getId(token), topicName, textName)
                    .map(Text::toEntityDTO)
            );
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * add a new topic for a selected user.
     * @param topicDTO a topic dto
     * @param token a user's jwt token
     * @return error if topic already exists
     */
    @PostMapping(value = "/addNewTopic", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNewTopic(
        final @RequestBody @NotNull TopicDTO topicDTO,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(EDIT_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        try {
            localTextService.addTopic(
                jwtTokenProvider.getId(token), jwtTokenProvider.getUsername(token), topicDTO.name()
            );
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (RestApiException e) {
            return ResponseEntity.status(e.getStatus()).body(e.toDTO());
        }
    }

    /**
     * add a new text for a selected topic.
     * @param textDTO a text  entity dto
     * @param token a user's jwt token
     * @return error if topic does not exist or test already exists
     */
    @PostMapping(value = "addNewText", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addNewText(
        final @RequestBody @NotNull TextEntityDTO textDTO,
        final @RequestParam @NotBlank String token
    ) {
        if (!jwtTokenProvider.validateToken(token)
            || !jwtTokenProvider.getPermissions(token).contains(EDIT_TEXT_PERMISSION)) {
            return new ResponseEntity<>(INVALID_TOKEN_ERROR_MESSAGE, HttpStatus.FORBIDDEN);
        }
        try {
            localTextService.addText(
                jwtTokenProvider.getId(token),
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
