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
    public static final RestApiException TEXT_FOUND_ERROR =
        new RestApiException("a text with this name already exists in the topic", HttpStatus.CONFLICT);
    public static final RestApiException TEXT_NOT_FOUND_ERROR =
        new RestApiException("a text is not found in this topic", HttpStatus.NOT_FOUND);
    public static final RestApiException TOPIC_OR_TEXT_NAME_NOT_FOUND_ERROR =
        new RestApiException("a topic or a text name is not found", HttpStatus.NOT_FOUND);
    public static final RestApiException OLD_TOPIC_NOT_FOUND_ERROR =
        new RestApiException("old topic not found", HttpStatus.NOT_FOUND);
    public static final RestApiException NEW_TOPIC_FOUND_ERROR =
        new RestApiException("new topic already exists", HttpStatus.NOT_FOUND);
    public static final RestApiException OLD_TEXT_NOT_FOUND_ERROR =
        new RestApiException("old text not found", HttpStatus.NOT_FOUND);
    public static final RestApiException NEW_TEXT_FOUND_ERROR =
        new RestApiException("new text already exists", HttpStatus.CONFLICT);

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
    private static final String HISTORY_MESSAGE_TEXTS_COUNT_PARAMETER = "tests_count";
    private static final String HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER = "old_topic_name";
    private static final String HISTORY_MESSAGE_NEW_TOPIC_NAME_PARAMETER = "new_topic_name";
    private static final String HISTORY_MESSAGE_UPDATE_TOPIC_STATUS_PARAMETER = "status";
    private static final String HISTORY_MESSAGE_OLD_TEXT_NAME_PARAMETER = "old_text_name";
    private static final String HISTORY_MESSAGE_NEW_TEXT_NAME_PARAMETER = "new_text_name";
    private static final String HISTORY_MESSAGE_OLD_TEXT_CONTENT_LENGTH_PARAMETER = "old_text_content_length";
    private static final String HISTORY_MESSAGE_NEW_TEXT_CONTENT_LENGTH_PARAMETER = "new_text_content_length";
    private static final String HISTORY_MESSAGE_UPDATE_TEXT_STATUS_PARAMETER = "status";
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
                "addText_textExistsError",
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
            throw TEXT_FOUND_ERROR;
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

    /**
     * update topic.
     * @param userId a user's id
     * @param topicOldName a current topic name
     * @param topicNewName a new topic name
     * @throws RestApiException an exception if it can not be executed
     */
    public void updateTopic(
        final @NotNull UUID userId,
        final @NotBlank String topicOldName,
        final @NotBlank String topicNewName
    ) throws RestApiException {
        final String updateTopicTableName = "updateTopic";
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicOldName)
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    updateTopicTableName,
                    Map.of(
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_ID_PARAMETER, -1,
                        HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER, topicOldName,
                        HISTORY_MESSAGE_NEW_TOPIC_NAME_PARAMETER, topicNewName,
                        HISTORY_MESSAGE_UPDATE_TOPIC_STATUS_PARAMETER, "oldTopicNotFound"
                    ),
                    Set.of(
                        HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER
                    )
                );
                return OLD_TOPIC_NOT_FOUND_ERROR;
            });

        if (topicRepository.findByUserIdAndName(userId, topicNewName).isPresent()) {
            usingHistory.sendMessage(
                updateTopicTableName,
                Map.of(
                    HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                    HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                    HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER, topicOldName,
                    HISTORY_MESSAGE_NEW_TOPIC_NAME_PARAMETER, topicNewName,
                    HISTORY_MESSAGE_UPDATE_TOPIC_STATUS_PARAMETER, "newTopicFound"
                ),
                Set.of(
                    HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER
                )
            );
            throw NEW_TOPIC_FOUND_ERROR;
        }

        topic.setName(topicNewName);
        topicRepository.save(topic);

        usingHistory.sendMessage(
            updateTopicTableName,
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER, topicOldName,
                HISTORY_MESSAGE_NEW_TOPIC_NAME_PARAMETER, topicNewName,
                HISTORY_MESSAGE_UPDATE_TOPIC_STATUS_PARAMETER, "success"
            ),
            Set.of(
                HISTORY_MESSAGE_OLD_TOPIC_NAME_PARAMETER
            )
        );
    }

    /**
     * update text.
     * @param userId a user's id
     * @param topicName a topic name
     * @param textOldName a current text anme
     * @param textNewName a new text name
     * @param textNewContent a new text content, Optional, empty if it is the same
     * @throws RestApiException an exception if it can not be executed
     */
    public void updateText(
        final @NotNull UUID userId,
        final @NotBlank String topicName,
        final @NotBlank String textOldName,
        final @NotBlank String textNewName,
        final @NotNull Optional<String> textNewContent
    ) throws RestApiException {
        final String updateTextTableName = "updateText";
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    updateTextTableName,
                    Map.of(
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_ID_PARAMETER, -1,
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                        HISTORY_MESSAGE_TEXT_ID_PARAMETER, -1,
                        HISTORY_MESSAGE_OLD_TEXT_NAME_PARAMETER, textOldName,
                        HISTORY_MESSAGE_NEW_TEXT_NAME_PARAMETER, textNewName,
                        HISTORY_MESSAGE_OLD_TEXT_CONTENT_LENGTH_PARAMETER, -1,
                        HISTORY_MESSAGE_NEW_TEXT_CONTENT_LENGTH_PARAMETER,
                            textNewContent.map(String::length).orElse(-1),
                        HISTORY_MESSAGE_UPDATE_TEXT_STATUS_PARAMETER, "topicNotFound"
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                    )
                );
                return TOPIC_NOT_FOUND_ERROR;
            });

        final Text text = textRepository.findByTopicAndName(topic, textOldName)
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    updateTextTableName,
                    Map.of(
                        HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                        HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                        HISTORY_MESSAGE_TEXT_ID_PARAMETER, -1,
                        HISTORY_MESSAGE_OLD_TEXT_NAME_PARAMETER, textOldName,
                        HISTORY_MESSAGE_NEW_TEXT_NAME_PARAMETER, textNewName,
                        HISTORY_MESSAGE_OLD_TEXT_CONTENT_LENGTH_PARAMETER, -1,
                        HISTORY_MESSAGE_NEW_TEXT_CONTENT_LENGTH_PARAMETER,
                            textNewContent.map(String::length).orElse(-1),
                        HISTORY_MESSAGE_UPDATE_TEXT_STATUS_PARAMETER, "textOldNotFound"
                    ),
                    Set.of(
                        HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                    )
                );
                return OLD_TEXT_NOT_FOUND_ERROR;
            });

        if (!textOldName.equals(textNewName) && textRepository.findByTopicAndName(topic, textNewName).isPresent()) {
            usingHistory.sendMessage(
                updateTextTableName,
                Map.of(
                    HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                    HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                    HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                    HISTORY_MESSAGE_TEXT_ID_PARAMETER, text.getId(),
                    HISTORY_MESSAGE_OLD_TEXT_NAME_PARAMETER, textOldName,
                    HISTORY_MESSAGE_NEW_TEXT_NAME_PARAMETER, textNewName,
                    HISTORY_MESSAGE_OLD_TEXT_CONTENT_LENGTH_PARAMETER, text.getText().length(),
                    HISTORY_MESSAGE_NEW_TEXT_CONTENT_LENGTH_PARAMETER,
                        textNewContent.map(String::length).orElse(-1),
                    HISTORY_MESSAGE_UPDATE_TEXT_STATUS_PARAMETER, "textNewFound"
                ),
                Set.of(
                    HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
                )
            );
            throw NEW_TEXT_FOUND_ERROR;
        }

        final Integer oldTextLength = text.getText().length();
        text.setName(textNewName);
        textNewContent.map(e -> {
            text.setText(e);
            kafkaTemplate.send("text", e);
            return e;
        });
        textRepository.save(text);

        usingHistory.sendMessage(
            "updateText",
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_ID_PARAMETER, topic.getId(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                HISTORY_MESSAGE_TEXT_ID_PARAMETER, text.getId(),
                HISTORY_MESSAGE_OLD_TEXT_NAME_PARAMETER, textOldName,
                HISTORY_MESSAGE_NEW_TEXT_NAME_PARAMETER, textNewName,
                HISTORY_MESSAGE_OLD_TEXT_CONTENT_LENGTH_PARAMETER, oldTextLength,
                HISTORY_MESSAGE_NEW_TEXT_CONTENT_LENGTH_PARAMETER,
                    textNewContent.map(String::length).orElse(-1),
                HISTORY_MESSAGE_UPDATE_TEXT_STATUS_PARAMETER, "success"
            ),
            Set.of(
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
            )
        );
    }

    /**
     * delete topic with all its texts.
     * @param userId a user's id
     * @param topicName a topic name
     * @throws RestApiException an exception if it can not be executed
     */
    public void deleteTopic(
        final @NotNull UUID userId,
        final @NotBlank String topicName
    ) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "deleteTopic_topicNotFoundError",
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

        final List<Text> texts = textRepository.findAllByTopic(topic);
        texts.forEach(e -> {
            textRepository.deleteById(e.getId());
        });
        topicRepository.deleteById(topic.getId());

        final Long cashId = getMostPopularWordsListForUserCashRepository.findByUserId(
            userId
        ).map(GetMostPopularWordsListForUserCash::getId).orElse(null);
        if (cashId != null) {
            getMostPopularWordsListForUserCashRepository.deleteById(cashId);
        }

        usingHistory.sendMessage(
            "deleteTopic",
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                HISTORY_MESSAGE_TEXTS_COUNT_PARAMETER, texts.size()
            ),
            Set.of(
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER
            )
        );
    }

    /**
     * delete a text.
     * @param userId a user's id
     * @param topicName a topic name
     * @param textName a text name
     * @throws RestApiException an exception if it can not be executed
     */
    public void deleteText(
        final @NotNull UUID userId,
        final @NotBlank String topicName,
        final @NotBlank String textName
    ) throws RestApiException {
        final Topic topic = topicRepository.findByUserIdAndName(userId, topicName)
            .orElseThrow(() -> {
                usingHistory.sendMessage(
                    "deleteText_topicNotFoundError",
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
        final Text text = textRepository.findByTopicAndName(topic, textName).orElseThrow(() -> {
            usingHistory.sendMessage(
                "deleteText_textNotFoundError",
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
            return TEXT_NOT_FOUND_ERROR;
        });

        textRepository.deleteById(text.getId());

        usingHistory.sendMessage(
            "deleteText",
            Map.of(
                HISTORY_MESSAGE_USER_ID_PARAMETER, userId.toString(),
                HISTORY_MESSAGE_TOPIC_NAME_PARAMETER, topicName,
                HISTORY_MESSAGE_TEXT_NAME_PARAMETER, textName,
                HISTORY_MESSAGE_TEXT_NAME_LENGTH_PARAMETER, textName.length(),
                HISTORY_MESSAGE_TEXT_LENGTH_PARAMETER, text.getText().length()
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
