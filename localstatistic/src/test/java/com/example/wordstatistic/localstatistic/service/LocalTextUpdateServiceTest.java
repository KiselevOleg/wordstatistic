package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.client.UsingHistoryService;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTextCash;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTopicCash;
import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForUserCash;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForTextCashRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForTopicCashRepository;
import com.example.wordstatistic.localstatistic.repository.redis.GetMostPopularWordsListForUserCashRepository;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalTextUpdateServiceTest {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;
    private final LocalTextService localTextService;

    @MockBean
    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    @MockBean
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    @MockBean
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;


    @MockBean
    private final UsingHistoryService usingHistory;

    @MockBean
    private final KafkaTemplate<String, String> kafkaTemplate;

    private UUID user1, user2, user3;

    @Autowired
    public LocalTextUpdateServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalTextService localTextService,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository,
        final UsingHistoryService usingHistory,
        final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localTextService = localTextService;
        this.getMostPopularWordsListForUserCashRepository = getMostPopularWordsListForUserCashRepository;
        this.getMostPopularWordsListForTopicCashRepository = getMostPopularWordsListForTopicCashRepository;
        this.getMostPopularWordsListForTextCashRepository = getMostPopularWordsListForTextCashRepository;
        this.usingHistory = usingHistory;
        this.kafkaTemplate = kafkaTemplate;
    }

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    @BeforeEach
    void setUp() {
        textRepository.deleteAll();
        topicRepository.deleteAll();

        user1 = UUID.randomUUID();
        user2 = UUID.randomUUID();
        user3 = UUID.randomUUID();
        topicRepository.save(new Topic(null, user1, "user1", "topic1"));
        topicRepository.save(new Topic(null, user2, "user2", "topic1"));
        topicRepository.save(new Topic(null, user2, "user2", "topic2"));
        topicRepository.save(new Topic(null, user3, "user3", "topic1"));
        topicRepository.save(new Topic(null, user3, "user3", "topic2"));
        topicRepository.save(new Topic(null, user3, "user3", "topic3"));

        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user1, "topic1").get(),
            "text111",
            "test text"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user1, "topic1").get(),
            "text112",
            "test text"
        ));

        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user2, "topic1").get(),
            "text211",
            "test text"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user2, "topic2").get(),
            "text221",
            "test text"
        ));

        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic1").get(),
            "text311",
            "test text"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic1").get(),
            "text312",
            "test text"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic1").get(),
            "text313",
            "test text"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic2").get(),
            "text321",
            "test text"
        ));
    }
    @AfterEach
    void tearDown() { }

    @Test
    public void updateTopicTest1() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );
        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.updateTopic(user3, "topic1", "topic1new");

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1new",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
            );
            e.getTopic().setName("topic1new");
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic1, e.getName()).get()
            );
            e.getTopic().setName("topic1");
        });
        assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
        textsTopic2.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic2, e.getName()).get()
            );
        });
        assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
        textsTopic3.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic3, e.getName()).get()
            );
        });

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "updateTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_id", topic1.getId(),
                "new_topic_name", "topic1new",
                "old_topic_name", "topic1",
                "status", "success"
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "old_topic_name"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void updateTopicTest2() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );
        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        try {
            localTextService.updateTopic(user3, "topic1old", "topic1new");
        } catch (RestApiException exception) {
            assertEquals("incorrect result", HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("incorrect result", "old topic not found", exception.getMessage());

            assertEquals(
                "incorrect result",
                Set.of(
                    "topic1",
                    "topic2",
                    "topic3"
                ),
                topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
            );
            assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
            textsTopic1.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
            textsTopic2.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic2, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
            textsTopic3.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic3, e.getName()).get()
                );
            });

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "updateTopic",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_id", -1,
                    "new_topic_name", "topic1new",
                    "old_topic_name", "topic1old",
                    "status", "oldTopicNotFound"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "old_topic_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }
        fail("incorrect result");
    }
    @Test
    public void updateTopicTest3() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );
        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        try {
            localTextService.updateTopic(user3, "topic1", "topic2");
        } catch (RestApiException exception) {
            assertEquals("incorrect result", HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("incorrect result", "new topic already exists", exception.getMessage());

            assertEquals(
                "incorrect result",
                Set.of(
                    "topic1",
                    "topic2",
                    "topic3"
                ),
                topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
            );
            assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
            textsTopic1.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
            textsTopic2.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic2, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
            textsTopic3.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic3, e.getName()).get()
                );
            });

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "updateTopic",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_id", topic1.getId(),
                    "new_topic_name", "topic2",
                    "old_topic_name", "topic1",
                    "status", "newTopicFound"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "old_topic_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }
        fail("incorrect result");
    }
    @Test
    public void updateTopicTest4() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );
        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        try {
            localTextService.updateTopic(user3, "topic1", "topic1");
        } catch (RestApiException exception) {
            assertEquals("incorrect result", HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("incorrect result", "new topic already exists", exception.getMessage());

            assertEquals(
                "incorrect result",
                Set.of(
                    "topic1",
                    "topic2",
                    "topic3"
                ),
                topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
            );
            assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
            textsTopic1.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
            textsTopic2.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic2, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
            textsTopic3.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic3, e.getName()).get()
                );
            });

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "updateTopic",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_id", topic1.getId(),
                    "new_topic_name", "topic1",
                    "old_topic_name", "topic1",
                    "status", "newTopicFound"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "old_topic_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }
        fail("incorrect result");
    }

    @Test
    public void updateTextTest1() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForUserCash(
                    3L, 3, user3,
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTopicCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTextCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(),
                        "text311"
                    ).get().getId(),
                    List.of(), 10L
                )
            )
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.updateText(
            user3, "topic1",
            "text311", "text311new",
            Optional.of("text new 123")
        );

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            if(!e.getName().equals("text311")) {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            } else {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, "text311new").isPresent()
                );
                e.setName("text311new");
                e.setText("text new 123");
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, "text311new").get()
                );
                e.setText("test text");
                e.setName("text311");
            }
        });
        assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
        textsTopic2.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic2, e.getName()).get()
            );
        });
        assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
        textsTopic3.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic3, e.getName()).get()
            );
        });

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForUserCashIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForTopicCashIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForTextCashIdCap.getValue()
        );

        assertEquals(
            "incorrectKafka",
            1,
            kafkaTemplateMessageTopic.getAllValues().size()
        );
        assertEquals(
            "incorrectKafka",
            "text",
            kafkaTemplateMessageTopic.getValue()
        );
        assertEquals(
            "incorrectKafka",
            "text new 123",
            kafkaTemplateMessageText.getValue()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "updateText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_id", topic1.getId(),
                "topic_name", "topic1",
                "text_id", textRepository.findByTopicAndName(topic1, "text311new").get().getId(),
                "new_text_name", "text311new",
                "old_text_name", "text311",
                "new_text_content_length", 12,
                "old_text_content_length", 9,
                "status", "success"
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "topic_name"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void updateTextTest2() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.empty()
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.empty()
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.empty()
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.updateText(
            user3, "topic1",
            "text311", "text311new",
            Optional.of("text new 123")
        );

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            if(!e.getName().equals("text311")) {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            } else {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, "text311new").isPresent()
                );
                e.setName("text311new");
                e.setText("text new 123");
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, "text311new").get()
                );
                e.setText("test text");
                e.setName("text311");
            }
        });
        assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
        textsTopic2.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic2, e.getName()).get()
            );
        });
        assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
        textsTopic3.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic3, e.getName()).get()
            );
        });

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrectKafka",
            1,
            kafkaTemplateMessageTopic.getAllValues().size()
        );
        assertEquals(
            "incorrectKafka",
            "text",
            kafkaTemplateMessageTopic.getValue()
        );
        assertEquals(
            "incorrectKafka",
            "text new 123",
            kafkaTemplateMessageText.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "updateText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_id", topic1.getId(),
                "topic_name", "topic1",
                "text_id", textRepository.findByTopicAndName(topic1, "text311new").get().getId(),
                "new_text_name", "text311new",
                "old_text_name", "text311",
                "new_text_content_length", 12,
                "old_text_content_length", 9,
                "status", "success"
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "topic_name"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void updateTextTest3() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.empty()
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTopicCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTextCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(),
                        "text311"
                    ).get().getId(),
                    List.of(), 10L
                )
            )
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.updateText(
            user3, "topic1",
            "text311", "text311new",
            Optional.empty()
        );

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            if(!e.getName().equals("text311")) {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            } else {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, "text311new").isPresent()
                );
                e.setName("text311new");
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, "text311new").get()
                );
                e.setName("text311");
            }
        });
        assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
        textsTopic2.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic2, e.getName()).get()
            );
        });
        assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
        textsTopic3.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic3, e.getName()).get()
            );
        });

        assertEquals(
            "incorrectKafka",
            0,
            kafkaTemplateMessageTopic.getAllValues().size()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForTopicCashIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForTextCashIdCap.getValue()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "updateText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_id", topic1.getId(),
                "topic_name", "topic1",
                "text_id", textRepository.findByTopicAndName(topic1, "text311new").get().getId(),
                "new_text_name", "text311new",
                "old_text_name", "text311",
                "new_text_content_length", -1,
                "old_text_content_length", 9,
                "status", "success"
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "topic_name"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void updateTextTest4() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForUserCash(
                    3L, 3, user3,
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTopicCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.empty()
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.updateText(
            user3, "topic1",
            "text311", "text311",
            Optional.of("text new 123")
        );

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            if(!e.getName().equals("text311")) {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            } else {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, "text311").isPresent()
                );
                e.setText("text new 123");
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, "text311").get()
                );
                e.setText("test text");
            }
        });
        assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
        textsTopic2.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic2, e.getName()).get()
            );
        });
        assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
        textsTopic3.forEach(e -> {
            assertEquals(
                "incorrect result",
                true,
                textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
            );
            assertEquals(
                "incorrect result",
                e,
                textRepository.findByTopicAndName(topic3, e.getName()).get()
            );
        });

        assertEquals(
            "incorrectKafka",
            1,
            kafkaTemplateMessageTopic.getAllValues().size()
        );
        assertEquals(
            "incorrectKafka",
            "text",
            kafkaTemplateMessageTopic.getValue()
        );
        assertEquals(
            "incorrectKafka",
            "text new 123",
            kafkaTemplateMessageText.getValue()
        );

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForUserCashIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            3L,
            getMostPopularWordsListForTopicCashIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "updateText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_id", topic1.getId(),
                "topic_name", "topic1",
                "text_id", textRepository.findByTopicAndName(topic1, "text311").get().getId(),
                "new_text_name", "text311",
                "old_text_name", "text311",
                "new_text_content_length", 12,
                "old_text_content_length", 9,
                "status", "success"
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "topic_name"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void updateTextTest5() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForUserCash(
                    3L, 3, user3,
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTopicCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTextCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(),
                        "text311"
                    ).get().getId(),
                    List.of(), 10L
                )
            )
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        try {
            localTextService.updateText(
                user3, "topic9",
                "text311", "text311new",
                Optional.of("text new 123")
            );
        } catch (RestApiException exception) {
            assertEquals("incorrect result", HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("incorrect result", "a topic is not found", exception.getMessage());

            assertEquals(
                "incorrect result",
                Set.of(
                    "topic1",
                    "topic2",
                    "topic3"
                ),
                topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
            );
            assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
            textsTopic1.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
            textsTopic2.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic2, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
            textsTopic3.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic3, e.getName()).get()
                );
            });

            assertEquals(
                "incorrectKafka",
                0,
                kafkaTemplateMessageTopic.getAllValues().size()
            );

            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForUserCashIdCap.getAllValues().size()
            );
            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
            );
            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForTextCashIdCap.getAllValues().size()
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "updateText",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_id", -1,
                    "topic_name", "topic9",
                    "text_id", -1,
                    "new_text_name", "text311new",
                    "old_text_name", "text311",
                    "new_text_content_length", 12,
                    "old_text_content_length", -1,
                    "status", "topicNotFound"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "topic_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }

        fail("incorrect result");
    }
    @Test
    public void updateTextTest6() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForUserCash(
                    3L, 3, user3,
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTopicCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTextCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(),
                        "text311"
                    ).get().getId(),
                    List.of(), 10L
                )
            )
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        try {
            localTextService.updateText(
                user3, "topic1",
                "text311old", "text311new",
                Optional.of("text new 123")
            );
        } catch (RestApiException exception) {
            assertEquals("incorrect result", HttpStatus.NOT_FOUND, exception.getStatus());
            assertEquals("incorrect result", "old text not found", exception.getMessage());

            assertEquals(
                "incorrect result",
                Set.of(
                    "topic1",
                    "topic2",
                    "topic3"
                ),
                topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
            );
            assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
            textsTopic1.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
            textsTopic2.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic2, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
            textsTopic3.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic3, e.getName()).get()
                );
            });

            assertEquals(
                "incorrectKafka",
                0,
                kafkaTemplateMessageTopic.getAllValues().size()
            );

            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForUserCashIdCap.getAllValues().size()
            );
            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
            );
            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForTextCashIdCap.getAllValues().size()
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "updateText",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_id", topic1.getId(),
                    "topic_name", "topic1",
                    "text_id", -1,
                    "new_text_name", "text311new",
                    "old_text_name", "text311old",
                    "new_text_content_length", 12,
                    "old_text_content_length", -1,
                    "status", "textOldNotFound"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "topic_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }

        fail("incorrect result");
    }
    @Test
    public void updateTextTest7() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForUserCash(
                    3L, 3, user3,
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTopicCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    List.of(), 10L
                )
            )
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTextCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user3,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic1").get(),
                "text311"
            ).get().getId()
        )).thenReturn(
            Optional.of(
                new GetMostPopularWordsListForTextCash(
                    3L, 3, user3,
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(),
                        "text311"
                    ).get().getId(),
                    List.of(), 10L
                )
            )
        );

        ArgumentCaptor<String> kafkaTemplateMessageTopic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaTemplateMessageText = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(
            kafkaTemplateMessageTopic.capture(),
            kafkaTemplateMessageText.capture()
        )).thenReturn(null);

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        try {
            localTextService.updateText(
                user3, "topic1",
                "text311", "text312",
                Optional.of("text new 123")
            );
        } catch (RestApiException exception) {
            assertEquals("incorrect result", HttpStatus.CONFLICT, exception.getStatus());
            assertEquals("incorrect result", "new text already exists", exception.getMessage());

            assertEquals(
                "incorrect result",
                Set.of(
                    "topic1",
                    "topic2",
                    "topic3"
                ),
                topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
            );
            assertEquals("incorrect result", textsTopic1.size(), textRepository.findAllByTopic(topic1).size());
            textsTopic1.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic1, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic2.size(), textRepository.findAllByTopic(topic2).size());
            textsTopic2.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic2, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic2, e.getName()).get()
                );
            });
            assertEquals("incorrect result", textsTopic3.size(), textRepository.findAllByTopic(topic3).size());
            textsTopic3.forEach(e -> {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic3, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    e,
                    textRepository.findByTopicAndName(topic3, e.getName()).get()
                );
            });

            assertEquals(
                "incorrectKafka",
                0,
                kafkaTemplateMessageTopic.getAllValues().size()
            );

            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForUserCashIdCap.getAllValues().size()
            );
            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
            );
            assertEquals(
                "incorrect cash",
                0,
                getMostPopularWordsListForTextCashIdCap.getAllValues().size()
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "updateText",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_id", topic1.getId(),
                    "topic_name", "topic1",
                    "text_id", textRepository.findByTopicAndName(topic1, "text311").get().getId(),
                    "new_text_name", "text312",
                    "old_text_name", "text311",
                    "new_text_content_length", 12,
                    "old_text_content_length", 9,
                    "status", "textNewFound"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "topic_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }

        fail("incorrect result");
    }
}
