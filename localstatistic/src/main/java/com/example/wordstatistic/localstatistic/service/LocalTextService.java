/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.client.UsingHistoryService;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTopicCash;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForUserCash;
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

import java.util.*;

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

    private static final String HISTORY_MESSAGE_ACCEPTED_PARAMETER = "accepted";
    private static final String HISTORY_MESSAGE_USER_ID_PARAMETER = "user_id";
    private static final String HISTORY_MESSAGE_USER_NAME_PARAMETER = "user_name";
    private static final String HISTORY_MESSAGE_TOPIC_ID_PARAMETER = "topic_id";
    private static final String HISTORY_MESSAGE_TOPIC_NAME_PARAMETER = "topic_name";
    private static final String HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER = "topic_name_length";
    private static final String HISTORY_MESSAGE_TEXT_NAME_PARAMETER = "text_name";
    private static final String HISTORY_MESSAGE_TEXT_NAME_LENGTH_PARAMETER = "text_name_length";
    private static final String HISTORY_MESSAGE_TEXT_ID_PARAMETER = "text_id";
    private static final String HISTORY_MESSAGE_TEXT_LENGTH_PARAMETER = "text_length";
    private final UsingHistoryService usingHistory;

    private final TextRepository textRepository;
    private final TopicRepository topicRepository;

    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    LocalTextService(
        final UsingHistoryService usingHistory,
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final KafkaTemplate<String, String> kafkaTemplate,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.usingHistory = usingHistory;
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
        final List<Topic> res = topicRepository.findAllByUserId(userId);
        usingHistory.sendMessage(
            "getAllTopicForUser",
            Map.of(
                HISTORY_MESSAGE_ACCEPTED_PARAMETER, res.size(),
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString()
            ),
            Set.of(
                HISTORY_MESSAGE_ACCEPTED_PARAMETER
            )
        );
        return res;
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
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "getAllTextsForSelectedTopic_topicNotFoundError",
                    Map.of(
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                    )
                );
                return TOPIC_NOT_FOUND_ERROR;
            });
        final List<Text> res = textRepository.findAllByTopic(topic);
        usingHistory.sendMessage(
            "getAllTextsForSelectedTopic",
            Map.of(
                HISTORY_MESSAGE_ACCEPTED_PARAMETER, res.size(),
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topic.getName()
            ),
            Set.of(
                HISTORY_MESSAGE_ACCEPTED_PARAMETER
            )
        );
        return res;
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
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "getTextForSelectedTextName_topicOrTextNotFoundError",
                    Map.of(
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                        HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER,
                        HISTORY_MESSAGE_TEXT_NAME_PARAMETER
                    )
                );
                return TOPIC_OR_TEXT_NAME_NOT_FOUND_ERROR;
            });
        final Optional<Text> res = textRepository.findByTopicAndName(topic, textName);
        usingHistory.sendMessage(
            "getTextForSelectedTextName",
            Map.of(
                HISTORY_MESSAGE_ACCEPTED_PARAMETER, res.map((e) -> "true").orElse("false"),
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topic.getName(),
                HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName,
                HISTORY_MESSAGE_TEXT_ID_PARAMETER, (res.map(Text::getId).orElse(-1)),
                HISTORY_MESSAGE_TEXT_LENGTH_PARAMETER, (res.map(text -> text.getText().length()).orElse(-1))
            ),
            Set.of(
                HISTORY_MESSAGE_ACCEPTED_PARAMETER
            )
        );
        return res;
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
            usingHistory.sendMessage(
                "addTopic_topicFoundError",
                Map.of(
                    HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                    HISTORY_MESSAGE_USER_NAME_PARAMETER, userName,
                    HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                    HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER, topicName.length()
                ),
                Set.of(
                    HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                )
            );
            throw TOPIC_FOUND_ERROR;
        }
        usingHistory.sendMessage(
            "addTopic",
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_USER_NAME_PARAMETER, userName,
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER, topicName.length()
            ),
            Set.of(
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
            )
        );
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
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "addText_topicNotFoundError",
                    Map.of(
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                        HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER, topicName.length(),
                        HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName,
                        HISTORY_MESSAGE_TEXT_NAME_LENGTH_PARAMETER, textName.length()
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                    )
                );
                return TOPIC_NOT_FOUND_ERROR;
            });
        if (textRepository.findByTopicAndName(topic, textName).isPresent()) {
            usingHistory.sendMessage(
                "addText_topicExistsError",
                Map.of(
                    HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                    HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                    HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER, topicName.length(),
                    HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName,
                    HISTORY_MESSAGE_TEXT_NAME_LENGTH_PARAMETER, textName.length()
                ),
                Set.of(
                    HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                )
            );
            throw TEXT_EXISTS_ERROR;
        }

        textRepository.save(new Text(null, topic, textName, text));
        kafkaTemplate.send("text", text);

        usingHistory.sendMessage(
            "addText",
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                HISTORY_MESSAGE_TOPIC_NAME_LENGTH_PARAMETER, topicName.length(),
                HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName,
                HISTORY_MESSAGE_TEXT_NAME_LENGTH_PARAMETER, textName.length()
            ),
            Set.of(
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
            )
        );

        Long cashId;
        cashId = getMostPopularWordsListForUserCashRepository.findByUserId(
            userId
        ).map(GetMostPopularWordsListForUserCash::getId).orElse(null);
        if (cashId != null) {
            getMostPopularWordsListForUserCashRepository.deleteById(cashId);
        }
        cashId = getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            userId, topic.getId()
        ).map(GetMostPopularWordsListForTopicCash::getId).orElse(null);
        if (cashId != null) {
            getMostPopularWordsListForTopicCashRepository.deleteById(cashId);
        }
    }
}
