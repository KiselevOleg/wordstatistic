package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.client.UsingHistoryService;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalTextViewServiceTest {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;
    private final LocalTextService localTextService;

    @MockBean
    private final UsingHistoryService usingHistory;

    private UUID user1, user2, user3;

    @Autowired
    public LocalTextViewServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalTextService localTextService,
        final UsingHistoryService usingHistory
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localTextService = localTextService;
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
    public void getAllTopicForUserTest1() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Topic> res = localTextService.getAllTopicForUser(user1);

        assertEquals("incorrect getting a list of topics", 1, res.size());
        final Topic rest = res.get(0);
        assertEquals("incorrect getting a list of topics", user1, rest.getUserId());
        assertEquals("incorrect getting a list of topics", "user1", rest.getUserName());
        assertEquals("incorrect getting a list of topics", "topic1", rest.getName());
        assertEquals("incorrect getting a list of topics",
            topicRepository.findByUserIdAndName(user1, "topic1").get(),
            rest);

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getAllTopicForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", 1,
                "user_id", usingHistoryParametersCap.getValue().get("user_id")
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
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getAllTopicForUserTest2() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Topic> res = localTextService.getAllTopicForUser(user3);

        assertEquals("incorrect getting a list of topics", 3, res.size());
        assertEquals("incorrect getting a list of topics",
            topicRepository.findByUserIdAndName(user3, res.get(0).getName()).get(),
            res.get(0));
        assertEquals("incorrect getting a list of topics",
            topicRepository.findByUserIdAndName(user3, res.get(1).getName()).get(),
            res.get(1));
        assertEquals("incorrect getting a list of topics",
            topicRepository.findByUserIdAndName(user3, res.get(2).getName()).get(),
            res.get(2));

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getAllTopicForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", 3,
                "user_id", usingHistoryParametersCap.getValue().get("user_id")
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
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getAllTopicForUserTest3() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final UUID userNew = UUID.randomUUID();
        List<Topic> res = localTextService.getAllTopicForUser(userNew);

        assertEquals("incorrect getting a list of topics", 0, res.size());

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getAllTopicForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", 0,
                "user_id", usingHistoryParametersCap.getValue().get("user_id")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            userNew,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }

    @Test
    public void getAllTextsForSelectedTopicTest1() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(user1, "topic1");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a list of texts", 2, res.size());
        assertEquals(
            "incorrect getting a list of texts",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user1, "topic1").get(), res.get(0).getName()
            ).get(),
            res.get(0)
        );
        assertEquals(
            "incorrect getting a list of texts",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user1, "topic1").get(), res.get(1).getName()
            ).get(),
            res.get(1)
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getAllTextsForSelectedTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", 2,
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "topic_id", usingHistoryParametersCap.getValue().get("topic_id")
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
            topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topic_id")
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getAllTextsForSelectedTopicTest2() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(user3, "topic2");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a list of texts", 1, res.size());
        assertEquals(
            "incorrect getting a list of texts",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user3, "topic2").get(), "text321"
            ).get(),
            res.get(0)
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getAllTextsForSelectedTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", 1,
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic2",
                "topic_id", usingHistoryParametersCap.getValue().get("topic_id")
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
            topicRepository.findByUserIdAndName(user3, "topic2").get().getId(),
            usingHistoryParametersCap.getValue().get("topic_id")
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getAllTextsForSelectedTopicTest3() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(user3, "topic3");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a list of texts", 0, res.size());

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getAllTextsForSelectedTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", 0,
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic3",
                "topic_id", usingHistoryParametersCap.getValue().get("topic_id")
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
            topicRepository.findByUserIdAndName(user3, "topic3").get().getId(),
            usingHistoryParametersCap.getValue().get("topic_id")
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getAllTextsForSelectedTopicTest4() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Text> res = null;
        final UUID userNew = UUID.randomUUID();
        try {
            res = localTextService.getAllTextsForSelectedTopic(userNew, "topic1");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic is not found", HttpStatus.NOT_FOUND),
                e
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "getAllTextsForSelectedTopic_topicNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic1"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                userNew,
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
    public void getAllTextsForSelectedTopicTest5() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(user1, "topic2");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic is not found", HttpStatus.NOT_FOUND),
                e
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "getAllTextsForSelectedTopic_topicNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic2"
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
    public void getTextForSelectedTextNameTest1() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        Optional<Text> res = Optional.empty();
        try {
            res = localTextService.getTextForSelectedTextName(user1, "topic1", "text112");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a text", true, res.isPresent());
        assertEquals(
            "incorrect getting a text",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user1, "topic1").get(),
                "text112"
            ).get(),
            res.get()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getTextForSelectedTextName",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", "true",
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "topic_id", usingHistoryParametersCap.getValue().get("topic_id"),
                "text_name", "text112",
                "text_id", usingHistoryParametersCap.getValue().get("text_id"),
                "text_length", 9
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
            topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topic_id")
        );
        assertEquals(
            "incorrect usingHistory",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user1, "topic1").get(),
                "text112"
            ).get().getId(),
            usingHistoryParametersCap.getValue().get("text_id")
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getTextForSelectedTextNameTest2() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        Optional<Text> res = Optional.empty();
        try {
            res = localTextService.getTextForSelectedTextName(user1, "topic1", "text116");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a text", true, res.isEmpty());

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "getTextForSelectedTextName",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "accepted", "false",
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "topic_id", usingHistoryParametersCap.getValue().get("topic_id"),
                "text_name", "text116",
                "text_id", usingHistoryParametersCap.getValue().get("text_id"),
                "text_length", -1
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
            topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topic_id")
        );
        assertEquals(
            "incorrect usingHistory",
            Set.of(
                "accepted"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getTextForSelectedTextNameTest3() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        Optional<Text> res = Optional.empty();
        try {
            res = localTextService.getTextForSelectedTextName(user1, "topic2", "text112");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic or a text name is not found", HttpStatus.NOT_FOUND),
                e
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "getTextForSelectedTextName_topicOrTextNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic2",
                    "text_name", "text112"
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
                    "topic_name", "text_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }
        fail("an expected exception has not been dropped");
    }
    @Test
    public void getTextForSelectedTextNameTest4() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        Optional<Text> res = Optional.empty();
        final UUID userNew = UUID.randomUUID();
        try {
            res = localTextService.getTextForSelectedTextName(userNew, "topic1", "text112");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic or a text name is not found", HttpStatus.NOT_FOUND),
                e
            );

            assertEquals(
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "getTextForSelectedTextName_topicOrTextNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic1",
                    "text_name", "text112"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                userNew,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("user_id"))
            );
            assertEquals(
                "incorrect usingHistory",
                Set.of(
                    "topic_name", "text_name"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }
        fail("an excepted exception has not been dropped");
    }
}
