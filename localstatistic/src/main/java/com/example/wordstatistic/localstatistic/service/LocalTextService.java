/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForTextCashRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForTopicCashRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForUserCashRepository;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class LocalTextService {
    public static final RestApiException TOPIC_NOT_FOUND_ERROR =
        new RestApiException("a topic is not found", HttpStatus.NOT_FOUND);
    public static final RestApiException TOPIC_FOUND_ERROR =
        new RestApiException("a topic already exists", HttpStatus.CONFLICT);
    public static final RestApiException TEXT_EXISTS_ERROR =
        new RestApiException("a text with this name already exists in the topic", HttpStatus.CONFLICT);
    public static final RestApiException TOPIC_OR_TEXT_NAME_NOT_FOUND_ERROR =
        new RestApiException("a topic or a text name is not found", HttpStatus.NOT_FOUND);

    private final TextRepository textRepository;
    private final TopicRepository topicRepository;

    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    LocalTextService(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final KafkaTemplate<String, String> kafkaTemplate,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.getMostPopularWordsListForUserCashRepository = getMostPopularWordsListForUserCashRepository;
        this.getMostPopularWordsListForTopicCashRepository = getMostPopularWordsListForTopicCashRepository;
        this.getMostPopularWordsListForTextCashRepository = getMostPopularWordsListForTextCashRepository;
    }

    /**
     * get all topics for a selected username.
     * @param userId the user's id
     * @return a list of topics
     */
    public List<Topic> getAllTopicForUser(final @NotNull UUID userId) {
        return topicRepository.findAllByUserId(userId);
    }

    /**
     * get lass text for a selected topic name.
     * @param userId a own the topic user's id
     * @param topicName the topic name
     * @return a list of texts
     */
    public List<Text> getAllTextsForSelectedTopic(
        final @NotNull UUID userId,
        final @NotBlank String topicName
    ) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> TOPIC_NOT_FOUND_ERROR);
        return textRepository.findAllByTopic(topic);
    }

    /**
     * get a text for a selected text name.
     * @param userId a own related topic user's name
     * @param topicName a containing the text topic's name
     * @param textName the topic name
     * @return a text object
     */
    public Optional<Text> getTextForSelectedTextName(
        final @NotNull UUID userId,
        final @NotBlank String topicName,
        final @NotBlank String textName
    ) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> TOPIC_OR_TEXT_NAME_NOT_FOUND_ERROR);
        return textRepository.findByTopicAndName(topic, textName);
    }

    /**
     * add a new topic.
     * @param userId a user's id
     * @param userName a owning username
     * @param topicName a new topic name
     */
    public void addTopic(
        final @NotNull UUID userId,
        final @NotBlank String userName,
        final @NotBlank String topicName
    ) throws RestApiException {
        if (topicRepository.findByUserIdAndName(userId, topicName).isPresent()) {
            throw TOPIC_FOUND_ERROR;
        }

        topicRepository.save(new Topic(null, userId, userName, topicName));
    }

    /**
     * add a new text.
     * @param userId a owning user's id
     * @param topicName a related topic's name
     * @param text the new text
     */
    public void addText(
        final @NotNull UUID userId,
        final @NotBlank String topicName,
        final @NotBlank String textName,
        final @NotBlank String text
    ) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> TOPIC_NOT_FOUND_ERROR);
        if (textRepository.findByTopicAndName(topic, textName).isPresent()) {
            throw TEXT_EXISTS_ERROR;
        }

        textRepository.save(new Text(null, topic, textName, text));
        kafkaTemplate.send("text", text);

        final Integer textId = textRepository.findByTopicAndName(topic, textName).orElseThrow().getId();
        getMostPopularWordsListForUserCashRepository.deleteByUserIdAndLimitLessThan(
            userId, Integer.MAX_VALUE
        );
        getMostPopularWordsListForTopicCashRepository.deleteByUserIdAndTopicIdLimitLessThan(
            userId, topic.getId(), Integer.MAX_VALUE
        );
        getMostPopularWordsListForTextCashRepository.deleteByUserIdAndTopicIdAndTextIdAndLimitLessThan(
            userId, topic.getId(), textId, Integer.MAX_VALUE
        );
    }
}
