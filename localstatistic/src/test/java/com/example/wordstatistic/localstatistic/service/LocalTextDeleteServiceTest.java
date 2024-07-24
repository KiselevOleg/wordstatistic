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
import java.util.stream.Collectors;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalTextDeleteServiceTest {
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
    public LocalTextDeleteServiceTest(
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
    public void deleteTopicTest1() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(new GetMostPopularWordsListForUserCash(
                1L,
                3,
                user3,
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
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(new GetMostPopularWordsListForTopicCash(
                2L,
                3,
                user3,
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
        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.deleteTopic(user3, "topic1");

        assertEquals(
            "incorrect result",
            Set.of(
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        textsTopic1.forEach(e -> {
            assertEquals(
                "incorrect result",
                false,
                textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
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
            0,
            getMostPopularWordsListForTopicCashIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "deleteTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "tests_count", 3
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
    public void deleteTopicTest2() throws RestApiException {
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

        localTextService.deleteTopic(user3, "topic1");

        assertEquals(
            "incorrect result",
            Set.of(
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        textsTopic1.forEach(e -> {
            assertEquals(
                "incorrect result",
                false,
                textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
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
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "deleteTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "tests_count", 3
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
    public void deleteTopicTest3() throws RestApiException {
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
            localTextService.deleteTopic(user3, "topic9");
        } catch (RestApiException exception) {
            assertEquals(
                "incorrect result", HttpStatus.NOT_FOUND, exception.getStatus()
            );
            assertEquals(
                "incorrect result", "a topic is not found", exception.getMessage()
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
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "deleteTopic_topicNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic9"
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
    public void deleteTextTest1() throws RestApiException {
        ArgumentCaptor<Long> getMostPopularWordsListForUserCashIdCap = ArgumentCaptor.forClass(Long.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteById(
            getMostPopularWordsListForUserCashIdCap.capture()
        );
        when(getMostPopularWordsListForUserCashRepository.findByUserId(user3)).thenReturn(
            Optional.of(new GetMostPopularWordsListForUserCash(
                1L,
                3,
                user3,
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
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(
            Optional.of(new GetMostPopularWordsListForTopicCash(
                2L,
                3,
                user3,
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
        final Topic topic1 = topicRepository.findByUserIdAndName(user3, "topic1").get();
        final Topic topic2 = topicRepository.findByUserIdAndName(user3, "topic2").get();
        final Topic topic3 = topicRepository.findByUserIdAndName(user3, "topic3").get();
        final List<Text> textsTopic1 = textRepository.findAllByTopic(topic1);
        final List<Text> textsTopic2 = textRepository.findAllByTopic(topic2);
        final List<Text> textsTopic3 = textRepository.findAllByTopic(topic3);

        localTextService.deleteText(user3, "topic1", "text311");

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size() - 1, textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            assertEquals(
                "incorrect result",
                !e.getName().equals("text311"),
                textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
            );
            if (!e.getName().equals("text311")) {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).get().equals(e)
                );
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
            "deleteText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "text_name", "text311",
                "text_name_length", 7,
                "text_length", 9
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
    public void deleteTextTest2() throws RestApiException {
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

        localTextService.deleteText(user3, "topic1", "text311");

        assertEquals(
            "incorrect result",
            Set.of(
                "topic1",
                "topic2",
                "topic3"
            ),
            topicRepository.findAllByUserId(user3).stream().map(e -> e.getName()).collect(Collectors.toSet())
        );
        assertEquals("incorrect result", textsTopic1.size() - 1, textRepository.findAllByTopic(topic1).size());
        textsTopic1.forEach(e -> {
            assertEquals(
                "incorrect result",
                !e.getName().equals("text311"),
                textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
            );
            if (!e.getName().equals("text311")) {
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).get().equals(e)
                );
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
            "incorrect usingHisory",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory",
            "deleteText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory",
            Map.of(
                "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                "topic_name", "topic1",
                "text_name", "text311",
                "text_name_length", 7,
                "text_length", 9
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
    public void deleteTextTest3() throws RestApiException {
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
            localTextService.deleteText(user3, "topic9", "text311");
        } catch (RestApiException exception) {
            assertEquals(
                "incorrect result", HttpStatus.NOT_FOUND, exception.getStatus()
            );
            assertEquals(
                "incorrect result", "a topic is not found", exception.getMessage()
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
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).get().equals(e)
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
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "deleteText_topicNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic9",
                    "topic_name_length", 6,
                    "text_name", "text311",
                    "text_name_length", 7
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
    public void deleteTextTest4() throws RestApiException {
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
            localTextService.deleteText(user3, "topic1", "text319");
        } catch (RestApiException exception) {
            assertEquals(
                "incorrect result", HttpStatus.NOT_FOUND, exception.getStatus()
            );
            assertEquals(
                "incorrect result", "a text is not found in this topic", exception.getMessage()
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
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).isPresent()
                );
                assertEquals(
                    "incorrect result",
                    true,
                    textRepository.findByTopicAndName(topic1, e.getName()).get().equals(e)
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
                "incorrect usingHisory",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect usingHistory",
                "deleteText_textNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect usingHistory",
                Map.of(
                    "user_id", usingHistoryParametersCap.getValue().get("user_id"),
                    "topic_name", "topic1",
                    "topic_name_length", 6,
                    "text_name", "text319",
                    "text_name_length", 7
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
