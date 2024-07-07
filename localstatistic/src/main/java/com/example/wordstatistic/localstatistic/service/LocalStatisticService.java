/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.util.WordStatisticStringAnalysis;
import com.example.wordstatistic.localstatistic.dto.WordDTO;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class LocalStatisticService {
    public static final RestApiException TOPIC_NOT_FOUND_ERROR =
        new RestApiException("a topic is not found", HttpStatus.NOT_FOUND);
    public static final RestApiException TEXT_NOT_FOUND_ERROR =
        new RestApiException("a text is not found", HttpStatus.NOT_FOUND);

    private final TextRepository textRepository;
    private final TopicRepository topicRepository;

    @Autowired
    public LocalStatisticService(final TextRepository textRepository, final TopicRepository topicRepository) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
    }

    /**
     * get a list of most popular words for all user's texts.
     * @param userId a user's id
     * @param limit limit of a list length
     * @return the result list
     */
    public List<WordDTO> getMostPopularWordsForUser(
        final @NotNull UUID userId,
        final @Min(1) Integer limit) {
        final Map<String, Integer> words = new HashMap<>();
        topicRepository.findAllByUserId(userId).forEach((topic) ->
                textRepository.findAllByTopic(topic)
                    .forEach((e) -> WordStatisticStringAnalysis.getAllWords(words, e.getText()))
        );

        List<WordDTO> listWords = words.entrySet()
            .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
        listWords = new ArrayList<>(listWords);
        listWords.sort((a, b) -> b.count() - a.count());
        return listWords.stream().limit(limit).toList();
    }

    /**
     * get a list of most popular words for all texts in a selected topic.
     * @param userId a user's id
     * @param topicName a topic's name
     * @param limit limit of a list length
     * @return the result list
     * @throws RestApiException an exception if input data is incorrect
     */
    public List<WordDTO> getMostPopularWordsForTopic(
        final @NotNull UUID userId,
        final @NotBlank String topicName,
        final @Min(1) Integer limit) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> TOPIC_NOT_FOUND_ERROR);

        final Map<String, Integer> words = new HashMap<>();
        textRepository.findAllByTopic(topic)
            .forEach((e) -> WordStatisticStringAnalysis.getAllWords(words, e.getText()));

        List<WordDTO> listWords = words.entrySet()
            .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
        listWords = new ArrayList<>(listWords);
        listWords.sort((a, b) -> b.count() - a.count());
        return listWords.stream().limit(limit).toList();
    }

    /**
     * get a list of most popular words for a selected text.
     * @param userId a user's id
     * @param topicName a topic's name
     * @param textName a text's name
     * @param limit limit of a list length
     * @return the result list
     * @throws RestApiException an exception if input data is incorrect
     */
    public List<WordDTO> getMostPopularWordsForText(
        final @NotNull UUID userId,
        final @NotBlank String topicName,
        final @NotBlank String textName,
        final @Min(1) Integer limit) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> TOPIC_NOT_FOUND_ERROR);
        final Text text = textRepository.findByTopicAndName(topic, textName)
            .orElseThrow(() -> TEXT_NOT_FOUND_ERROR);

        final Map<String, Integer> words = WordStatisticStringAnalysis.getAllWords(text.getText());

        List<WordDTO> listWords = words.entrySet()
            .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
        listWords = new ArrayList<>(listWords);
        listWords.sort((a, b) -> b.count() - a.count());
        return listWords.stream().limit(limit).toList();
    }
}
