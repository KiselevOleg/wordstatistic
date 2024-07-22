/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.client.UsingHistoryService;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
@SuppressWarnings("PMD.ClassFanOutComplexity")
public class LocalStatisticService {
    public static final RestApiException TOPIC_NOT_FOUND_ERROR =
        new RestApiException("a topic is not found", HttpStatus.NOT_FOUND);
    public static final RestApiException TEXT_NOT_FOUND_ERROR =
        new RestApiException("a text is not found", HttpStatus.NOT_FOUND);

    private static final String HISTORY_MESSAGE_LIMIT_PARAMETER = "limit";
    private static final String HISTORY_MESSAGE_ACCEPTED_PARAMETER = "accepted";
    private static final String HISTORY_MESSAGE_USER_ID_PARAMETER = "userId";
    private static final String HISTORY_MESSAGE_TOPIC_ID_PARAMETER = "topicId";
    private static final String HISTORY_MESSAGE_TOPIC_NAME_PARAMETER = "topicName";
    private static final String HISTORY_MESSAGE_TEXT_ID_PARAMETER = "textId";
    private static final String HISTORY_MESSAGE_TEXT_NAME_PARAMETER = "textName";
    private final UsingHistoryService usingHistory;

    private final TextRepository textRepository;
    private final TopicRepository topicRepository;

    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;

    @Autowired
    public LocalStatisticService(
        final UsingHistoryService usingHistory,
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.usingHistory = usingHistory;
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
        final AtomicReference<Optional<Long>> cashId = new AtomicReference<>(Optional.empty());
        return getMostPopularWordsListForUserCashRepository
            .findByUserId(userId)
            .map(e -> {
                cashId.set(Optional.of(e.getId()));
                return e;
            })
            .filter(e -> e.getLimit() >= limit)
            .map(e -> {
                e.setResult(e.getResult().stream().limit(limit).toList());
                return e.getResult();
            })
            .or(() -> {
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

                cashId.get().map(e -> {
                    getMostPopularWordsListForUserCashRepository.deleteById(e);
                    return e;
                });
                getMostPopularWordsListForUserCashRepository.save(
                    new GetMostPopularWordsListForUserCash(
                        null, limit, userId, list, null
                    )
                );
                return Optional.of(list);
            })
            .map((e) -> {
                usingHistory.sendMessage(
                    "getMostPopularWordsForUser",
                    Map.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER, limit,
                        HISTORY_MESSAGE_ACCEPTED_PARAMETER, e.size(),
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString()
                    ),
                    Set.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER
                    )
                );
                return e;
            }).orElseThrow();
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
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "getMostPopularWordsForTopic_topicNotFoundError",
                    Map.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER, limit,
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                    )
                );
                return TOPIC_NOT_FOUND_ERROR;
            });
        final AtomicReference<Optional<Long>> cashId = new AtomicReference<>(Optional.empty());
        return getMostPopularWordsListForTopicCashRepository
            .findByUserIdAndTopicId(userId, topic.getId())
            .map(e -> {
                cashId.set(Optional.of(e.getId()));
                return e;
            })
            .filter(e -> e.getLimit() >= limit)
            .map(e -> {
                e.setResult(e.getResult().stream().limit(limit).toList());
                return e.getResult();
            })
            .or(() -> {
                final Map<String, Integer> words = new HashMap<>();
                textRepository.findAllByTopic(topic)
                    .forEach((e) -> WordStatisticStringAnalysis.getAllWords(words, e.getText()));

                List<WordDTO> list = words.entrySet()
                    .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
                list = new ArrayList<>(list);
                list.sort((a, b) -> b.count() - a.count());
                list = list.stream().limit(limit).toList();

                cashId.get().map(e -> {
                    getMostPopularWordsListForTopicCashRepository.deleteById(e);
                    return e;
                });
                getMostPopularWordsListForTopicCashRepository.save(
                    new GetMostPopularWordsListForTopicCash(
                        null, limit, userId, topic.getId(), list, null
                    )
                );
                return Optional.of(list);
            })
            .map((e) -> {
                usingHistory.sendMessage(
                    "getMostPopularWordsForTopic",
                    Map.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER, limit,
                        HISTORY_MESSAGE_ACCEPTED_PARAMETER, e.size(),
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topic.getName()
                    ),
                    Set.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER
                    )
                );
                return e;
            }).orElseThrow();
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
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "getMostPopularWordsForText_TopicNotFoundError",
                    Map.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER, limit,
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                        HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, HISTORY_MESSAGE_TEXT_NAME_PARAMETER
                    )
                );
                return TOPIC_NOT_FOUND_ERROR;
            });
        final Text text = textRepository.findByTopicAndName(topic, textName)
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "getMostPopularWordsForText_TextNotFoundError",
                    Map.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER, limit,
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topic.getName(),
                        HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                        HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, HISTORY_MESSAGE_TEXT_NAME_PARAMETER
                    )
                );
                return TEXT_NOT_FOUND_ERROR;
            });
        final AtomicReference<Optional<Long>> cashId = new AtomicReference<>(Optional.empty());
        return getMostPopularWordsListForTextCashRepository
            .findByUserIdAndTopicIdAndTextId(userId, topic.getId(), text.getId())
            .map(e -> {
                cashId.set(Optional.of(e.getId()));
                return e;
            })
            .filter(e -> e.getLimit() >= limit)
            .map(e -> {
                e.setResult(e.getResult().stream().limit(limit).toList());
                return e.getResult();
            })
            .or(() -> {
                final Map<String, Integer> words = WordStatisticStringAnalysis.getAllWords(text.getText());

                List<WordDTO> list = words.entrySet()
                    .stream().map((e) -> new WordDTO(e.getKey(), e.getValue())).toList();
                list = new ArrayList<>(list);
                list.sort((a, b) -> b.count() - a.count());
                list = list.stream().limit(limit).toList();

                cashId.get().map(e -> {
                    getMostPopularWordsListForTextCashRepository.deleteById(e);
                    return e;
                });
                getMostPopularWordsListForTextCashRepository.save(
                    new GetMostPopularWordsListForTextCash(
                        null, limit, userId, topic.getId(), text.getId(), list, null
                    )
                );
                return Optional.of(list);
            })
            .map((e) -> {
                usingHistory.sendMessage(
                    "getMostPopularWordsForText",
                    Map.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER, limit,
                        HISTORY_MESSAGE_ACCEPTED_PARAMETER, e.size(),
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topic.getName(),
                        HISTORY_MESSAGE_TEXT_ID_PARAMETER, text.getId(),
                        HISTORY_MESSAGE_TEXT_NAME_PARAMETER, text.getName()
                    ),
                    Set.of(
                        HISTORY_MESSAGE_LIMIT_PARAMETER
                    )
                );
                return e;
            }).orElseThrow();
    }
}
