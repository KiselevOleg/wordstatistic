package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalTextServiceTest {
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
    private KafkaTemplate<String, String> kafkaTemplate;

    private UUID user1, user2, user3;

    @Autowired
    public LocalTextServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalTextService localTextService,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localTextService = localTextService;
        this.getMostPopularWordsListForUserCashRepository = getMostPopularWordsListForUserCashRepository;
        this.getMostPopularWordsListForTopicCashRepository = getMostPopularWordsListForTopicCashRepository;
        this.getMostPopularWordsListForTextCashRepository = getMostPopularWordsListForTextCashRepository;
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
        List<Topic> res = localTextService.getAllTopicForUser(user1);

        assertEquals("incorrect getting a list of topics", 1, res.size());
        final Topic rest = res.get(0);
        assertEquals("incorrect getting a list of topics", user1, rest.getUserId());
        assertEquals("incorrect getting a list of topics", "user1", rest.getUserName());
        assertEquals("incorrect getting a list of topics", "topic1", rest.getName());
        assertEquals("incorrect getting a list of topics",
            topicRepository.findByUserIdAndName(user1, "topic1").get(),
            rest);
    }
    @Test
    public void getAllTopicForUserTest2() {
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
    }
    @Test
    public void getAllTopicForUserTest3() {
        List<Topic> res = localTextService.getAllTopicForUser(UUID.randomUUID());

        assertEquals("incorrect getting a list of topics", 0, res.size());
    }

    @Test
    public void getAllTextsForSelectedTopicTest1() {
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
    }
    @Test
    public void getAllTextsForSelectedTopicTest2() {
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
    }
    @Test
    public void getAllTextsForSelectedTopicTest3() {
        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(user3, "topic3");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a list of texts", 0, res.size());
    }
    @Test
    public void getAllTextsForSelectedTopicTest4() {
        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(UUID.randomUUID(), "topic1");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic is not found", HttpStatus.NOT_FOUND),
                e
            );
            return;
        }
        fail("an excepted exception has not been dropped");
    }
    @Test
    public void getAllTextsForSelectedTopicTest5() {
        List<Text> res = null;
        try {
            res = localTextService.getAllTextsForSelectedTopic(user1, "topic2");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic is not found", HttpStatus.NOT_FOUND),
                e
            );
            return;
        }
        fail("an excepted exception has not been dropped");
    }
    @Test
    public void getTextForSelectedTextNameTest1() {
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
    }
    @Test
    public void getTextForSelectedTextNameTest2() {
        Optional<Text> res = Optional.empty();
        try {
            res = localTextService.getTextForSelectedTextName(user1, "topic1", "text116");
        } catch (RestApiException e) {
            fail("an unexpected exception");
        }

        assertEquals("incorrect getting a text", true, res.isEmpty());
    }
    @Test
    public void getTextForSelectedTextNameTest3() {
        Optional<Text> res = Optional.empty();
        try {
            res = localTextService.getTextForSelectedTextName(user1, "topic2", "text112");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic or a text name is not found", HttpStatus.NOT_FOUND),
                e
            );

            return;
        }
        fail("an expected exception has not been dropped");
    }
    @Test
    public void getTextForSelectedTextNameTest4() {
        Optional<Text> res = Optional.empty();
        try {
            res = localTextService.getTextForSelectedTextName(UUID.randomUUID(), "topic1", "text112");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic or a text name is not found", HttpStatus.NOT_FOUND),
                e
            );

            return;
        }
        fail("an excepted exception has not been dropped");
    }

    @Test
    public void addTopicTest1() {
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
    }
    @Test
    public void addTopicTest2() {
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
    }
    @Test
    public void addTopicTest3() {
        try {
            localTextService.addTopic(user1, "user1", "topic1");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic already exists", HttpStatus.CONFLICT),
                e
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
        ArgumentCaptor<UUID> getMostPopularWordsListForUserCashUserIdCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> getMostPopularWordsListForUserCashLimitCap = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteByUserIdAndLimitLessThan(
            getMostPopularWordsListForUserCashUserIdCap.capture(),
            getMostPopularWordsListForUserCashLimitCap.capture()
        );
        ArgumentCaptor<UUID> getMostPopularWordsListForTopicCashUserIdCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> getMostPopularWordsListForTopicCashTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> getMostPopularWordsListForTopicCashLimitCap = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteByUserIdAndTopicIdLimitLessThan(
            getMostPopularWordsListForTopicCashUserIdCap.capture(),
            getMostPopularWordsListForTopicCashTopicIdCap.capture(),
            getMostPopularWordsListForTopicCashLimitCap.capture()
        );
        ArgumentCaptor<UUID> getMostPopularWordsListForTextCashUserIdCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> getMostPopularWordsListForTextCashTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> getMostPopularWordsListForTextCashTextIdCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> getMostPopularWordsListForTextCashLimitCap = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteByUserIdAndTopicIdAndTextIdAndLimitLessThan(
            getMostPopularWordsListForTextCashUserIdCap.capture(),
            getMostPopularWordsListForTextCashTopicIdCap.capture(),
            getMostPopularWordsListForTextCashTextIdCap.capture(),
            getMostPopularWordsListForTextCashLimitCap.capture()
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
            getMostPopularWordsListForUserCashUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            Integer.MAX_VALUE,
            getMostPopularWordsListForUserCashLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForTopicCashUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            Integer.MAX_VALUE,
            getMostPopularWordsListForTopicCashLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForTextCashUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
            getMostPopularWordsListForTextCashTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user1, "topic1").get(),
                "text113"
            ).get().getId(),
            getMostPopularWordsListForTextCashTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            Integer.MAX_VALUE,
            getMostPopularWordsListForTextCashLimitCap.getValue()
        );
    }
    @Test
    public void addTextTest2() {
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
    }
    @Test
    public void addTextTest3() {
        try {
            localTextService.addText(user1, "topic1", "text111", "a new text");
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a text with this name already exists in the topic", HttpStatus.CONFLICT),
                e
            );

            return;
        }

        fail("an expected exception has not been dropped");
    }
}
