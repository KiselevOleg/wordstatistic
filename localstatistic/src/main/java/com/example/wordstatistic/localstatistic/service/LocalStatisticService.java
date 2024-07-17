/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTextCash;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTopicCash;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForUserCash;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForTextCashRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForTopicCashRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForUserCashRepository;
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

    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;

    @Autowired
    public LocalStatisticService(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.getMostPopularWordsListForUserCashRepository = getMostPopularWordsListForUserCashRepository;
        this.getMostPopularWordsListForTopicCashRepository = getMostPopularWordsListForTopicCashRepository;
        this.getMostPopularWordsListForTextCashRepository = getMostPopularWordsListForTextCashRepository;
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
        return getMostPopularWordsListForUserCashRepository
            .findByUserId(userId)
            .filter(e -> e.getLimit() >= limit)
            .map(e -> {
                e.setResult(e.getResult().stream().limit(limit).toList());
                return e.getResult();
            })
            .orElseGet(() -> {
                final Map<String, Integer> words = new HashMap<>();
                topicRepository.findAllByUserId(userId).forEach((topic) ->
                    textRepository.findAllByTopic(topic)
                        .forEach((e) -> WordStatisticStringAnalysis.getAllWords(words, e.getText()))
                );
                List<WordDTO> list = words.entrySet()
                        .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
                list = new ArrayList<>(list);
                list.sort((a, b) -> b.count() - a.count());
                list = list.stream().limit(limit).toList();

                getMostPopularWordsListForUserCashRepository.deleteByUserIdAndLimitLessThan(
                    userId, limit
                );
                getMostPopularWordsListForUserCashRepository.save(
                    new GetMostPopularWordsListForUserCash(
                        null, limit, userId, list, null
                    )
                );
                return list;
            });
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
        return getMostPopularWordsListForTopicCashRepository
            .findByUserIdAndTopicId(userId, topic.getId())
            .filter(e -> e.getLimit() >= limit)
            .map(e -> {
                e.setResult(e.getResult().stream().limit(limit).toList());
                return e.getResult();
            })
            .orElseGet(() -> {
                final Map<String, Integer> words = new HashMap<>();
                textRepository.findAllByTopic(topic)
                    .forEach((e) -> WordStatisticStringAnalysis.getAllWords(words, e.getText()));

                List<WordDTO> list = words.entrySet()
                    .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
                list = new ArrayList<>(list);
                list.sort((a, b) -> b.count() - a.count());
                list = list.stream().limit(limit).toList();

                getMostPopularWordsListForTopicCashRepository.deleteByUserIdAndTopicIdLimitLessThan(
                    userId, topic.getId(), limit
                );
                getMostPopularWordsListForTopicCashRepository.save(
                    new GetMostPopularWordsListForTopicCash(
                        null, limit, userId, topic.getId(), list, null
                    )
                );
                return list;
            });
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
        return getMostPopularWordsListForTextCashRepository
            .findByUserIdAndTopicIdAndTextId(userId, topic.getId(), text.getId())
            .filter(e -> e.getLimit() >= limit)
            .map(e -> {
                e.setResult(e.getResult().stream().limit(limit).toList());
                return e.getResult();
            })
            .orElseGet(() -> {
                final Map<String, Integer> words = WordStatisticStringAnalysis.getAllWords(text.getText());

                List<WordDTO> list = words.entrySet()
                    .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
                list = new ArrayList<>(list);
                list.sort((a, b) -> b.count() - a.count());
                list = list.stream().limit(limit).toList();

                getMostPopularWordsListForTextCashRepository.deleteByUserIdAndTopicIdAndTextIdAndLimitLessThan(
                    userId, topic.getId(), text.getId(), limit
                );
                getMostPopularWordsListForTextCashRepository.save(
                    new GetMostPopularWordsListForTextCash(
                        null, limit, userId, topic.getId(), text.getId(), list, null
                    )
                );
                return list;
            });
    }
}
