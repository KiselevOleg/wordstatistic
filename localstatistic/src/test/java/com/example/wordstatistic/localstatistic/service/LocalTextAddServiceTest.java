package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.client.UsingHistoryService;
import com.example.wordstatistic.localstatistic.dto.WordDTO;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalTextAddServiceTest {
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
    private final KafkaTemplate<String, String> kafkaTemplate;
    @MockBean
    private final UsingHistoryService usingHistory;

    private UUID user1, user2, user3;

    @Autowired
    public LocalTextAddServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalTextService localTextService,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository,
        final KafkaTemplate<String, String> kafkaTemplate,
        final UsingHistoryService usingHistory
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localTextService = localTextService;
        this.getMostPopularWordsListForUserCashRepository = getMostPopularWordsListForUserCashRepository;
        this.getMostPopularWordsListForTopicCashRepository = getMostPopularWordsListForTopicCashRepository;
        this.getMostPopularWordsListForTextCashRepository = getMostPopularWordsListForTextCashRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.usingHistory = usingHistory;
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
    public void addTopicTest1() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localTextService.addTopic(user1, "user1", "topic2");
        } catch (RestApiException e) {
            fail("unexpected exception");
        }

        assertEquals("incorrect getting a new topic", 2, topicRepository.findAllByUserId(user1).size());
        Optional<Topic> res = topicRepository.findByUserIdAndName(user1, "topic2");
        assertEquals("incorrect getting a new topic", true, res.isPresent());
        assertEquals("incorrect getting a new topic", user1, res.get().getUserId());
        assertEquals("incorrect getting a new topic", "user1", res.get().getUserName());
        assertEquals("incorrect getting a new topic", "topic2", res.get().getName());

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "addTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "user_name", "user1",
                "topic_name", "topic2",
                "topic_name_length", 6
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user1,
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
    public void addTopicTest2() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        UUID user1 = UUID.randomUUID();
        try {
            localTextService.addTopic(user1, "user1", "topic2");
        } catch (RestApiException e) {
            fail("unexpected exception");
        }

        assertEquals("incorrect getting a new topic", 1, topicRepository.findAllByUserId(user1).size());
        Optional<Topic> res = topicRepository.findByUserIdAndName(user1, "topic2");
        assertEquals("incorrect getting a new topic", true, res.isPresent());
        assertEquals("incorrect getting a new topic", user1, res.get().getUserId());
        assertEquals("incorrect getting a new topic", "user1", res.get().getUserName());
        assertEquals("incorrect getting a new topic", "topic2", res.get().getName());

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "addTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "user_name", "user1",
                "topic_name", "topic2",
                "topic_name_length", 6
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user1,
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
    public void addTopicTest3() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localTextService.addTopic(user1, "user1", "topic1");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic already exists", HttpStatus.CONFLICT),
                e
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "addTopic_topicFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "user_name", "user1",
                    "topic_name", "topic1",
                    "topic_name_length", 6
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user1,
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

        fail("an excepted exception has not been dropped");
    }

    @Test
    public void addTextTest1() {
        ArgumentCaptor<String> kafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> kafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(kafkaTopicCap.capture(), kafkaMessageCap.capture())).thenReturn(null);

        //GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
        //GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
        //GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user1)).thenReturn(
            Optional.of(new GetMostPopularWordsListForUserCash(
                1L,
                3,
                user1,
                List.of(
                    new WordDTO("a", 2),
                    new WordDTO("b", 1),
                    new WordDTO("c", 1)),
                null
            ))
        );
        ArgumentCaptor<Long> getMostPopularWordsListForTopicCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user1, topicRepository.findByUserIdAndName(user1, "topic1").get().getId()
        )).thenReturn(
            Optional.of(new GetMostPopularWordsListForTopicCash(
                2L,
                3,
                user1,
                topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
                List.of(
                    new WordDTO("a", 2),
                    new WordDTO("b", 1),
                    new WordDTO("c", 1)),
                null
            ))
        );

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localTextService.addText(user1, "topic1", "text113", "a new text");
        } catch (RestApiException e) {
            fail("unexpected exception");
        }

        Topic topic = topicRepository.findByUserIdAndName(user1, "topic1").get();
        assertEquals("incorrect getting a new text", 3, textRepository.findAllByTopic(topic).size());
        Optional<Text> res = textRepository.findByTopicAndName(topic, "text113");
        assertEquals("incorrect getting a new text", true, res.isPresent());
        assertEquals("incorrect getting a new text", topic, res.get().getTopic());
        assertEquals("incorrect getting a new text", "text113", res.get().getName());
        assertEquals("incorrect getting a new text", "a new text", res.get().getText());

        assertEquals("kafka message incorrect", "text", kafkaTopicCap.getValue());
        assertEquals("kafka message incorrect", "a new text", kafkaMessageCap.getValue());

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            1L,
            getMostPopularWordsListForUserCashIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            2L,
            getMostPopularWordsListForTopicCashIdCap.getValue()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "addText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "topic_name_length", 6,
                "text_name", "text113",
                "text_name_length", 7
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user1,
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
    public void addTextTest2() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localTextService.addText(user1, "topic2", "text111", "a new text");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic is not found", HttpStatus.NOT_FOUND),
                e
            );

            return;
        }

        fail("an expected exception has not been dropped");

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "addText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "topic_name_length", 6,
                "text_name", "text113",
                "text_name_length", 7
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            user1,
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
    public void addTextTest3() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localTextService.addText(user1, "topic1", "text111", "a new text");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a text with this name already exists in the topic", HttpStatus.CONFLICT),
                e
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "addText_textExistsError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic1",
                    "topic_name_length", 6,
                    "text_name", "text111",
                    "text_name_length", 7
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                user1,
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

        fail("an expected exception has not been dropped");
    }
}
