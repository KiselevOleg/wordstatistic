package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GettingMessagesFromOtherServicesServiceTest {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;
    private final GettingMessagesFromOtherServicesService gettingMessagesFromOtherServicesService;

    private UUID user1, user2, user3;

    @Autowired
    public GettingMessagesFromOtherServicesServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final GettingMessagesFromOtherServicesService gettingMessagesFromOtherServicesService
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.gettingMessagesFromOtherServicesService = gettingMessagesFromOtherServicesService;
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
    public void changeUsernameTest1() throws KafkaDTOException, JsonProcessingException {
        gettingMessagesFromOtherServicesService.changeUsername(
            "{\"userId\":\"" + user3 + "\",\"newUsername\":\"user3new\"}"
        );

        Set<Topic> topics = new HashSet<>();
        topicRepository.findAll().forEach(topics::add);
        Set<Text> texts = new HashSet<>();
        textRepository.findAll().forEach(texts::add);

        assertEquals(
            "incorrect result",
            Set.of(
                new Topic(
                    topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
                    user1, "user1", "topic1"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
                    user2, "user2", "topic1"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user2, "topic2").get().getId(),
                    user2, "user2", "topic2"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                    user3, "user3new", "topic1"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user3, "topic2").get().getId(),
                    user3, "user3new", "topic2"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user3, "topic3").get().getId(),
                    user3, "user3new", "topic3"
                )
            ),
            topics
        );
        assertEquals(
            "incorrect result",
            Set.of(
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user1, "topic1").get(), "text111"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user1, "topic1").get(),
                    "text111",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user1, "topic1").get(), "text112"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user1, "topic1").get(),
                    "text112",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user2, "topic1").get(),
                    "text211",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user2, "topic2").get(), "text221"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user2, "topic2").get(),
                    "text221",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(), "text311"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user3, "topic1").get(),
                    "text311",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(), "text312"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user3, "topic1").get(),
                    "text312",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic1").get(), "text313"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user3, "topic1").get(),
                    "text313",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user3, "topic2").get(), "text321"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user3, "topic2").get(),
                    "text321",
                    "test text"
                )
            ),
            texts
        );
    }

    @Test
    public void deleteUserTest1() throws KafkaDTOException, JsonProcessingException {
        gettingMessagesFromOtherServicesService.deleteUser(
            "{\"userId\":\"" + user3 + "\"}"
        );

        Set<Topic> topics = new HashSet<>();
        topicRepository.findAll().forEach(topics::add);
        Set<Text> texts = new HashSet<>();
        textRepository.findAll().forEach(texts::add);

        assertEquals(
            "incorrect result",
            Set.of(
                new Topic(
                    topicRepository.findByUserIdAndName(user1, "topic1").get().getId(),
                    user1, "user1", "topic1"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
                    user2, "user2", "topic1"
                ),
                new Topic(
                    topicRepository.findByUserIdAndName(user2, "topic2").get().getId(),
                    user2, "user2", "topic2"
                )
            ),
            topics
        );
        assertEquals(
            "incorrect result",
            Set.of(
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user1, "topic1").get(), "text111"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user1, "topic1").get(),
                    "text111",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user1, "topic1").get(), "text112"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user1, "topic1").get(),
                    "text112",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user2, "topic1").get(),
                    "text211",
                    "test text"
                ),
                new Text(
                    textRepository.findByTopicAndName(
                        topicRepository.findByUserIdAndName(user2, "topic2").get(), "text221"
                    ).get().getId(),
                    topicRepository.findByUserIdAndName(user2, "topic2").get(),
                    "text221",
                    "test text"
                )
            ),
            texts
        );
    }
}
