package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.dto.WordDTO;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.repository.TextRepository;
import com.example.wordstatistic.localstatistic.repository.TopicRepository;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalStatisticServiceTest {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;
    private final LocalStatisticService localStatisticService;

    private UUID user1, user2, user3;

    @Autowired
    public LocalStatisticServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalStatisticService localStatisticService
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localStatisticService = localStatisticService;
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
            "a test text"
        ));

        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user2, "topic1").get(),
            "text211",
            "text text text test test a"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user2, "topic2").get(),
            "text221",
            "test"
        ));

        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic1").get(),
            "text311",
            "test"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic1").get(),
            "text312",
            "text"
        ));
        textRepository.save(new Text(
            null,
            topicRepository.findByUserIdAndName(user3, "topic1").get(),
            "text313",
            "text"
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
    public void getMostPopularWordsForUserTest1() {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForUser(user1, 2);

        assertEquals("incorrect statistic", 2, res.size());

        assertEquals(
            "incorrect statistic",
            true,
            res.get(0).name().equals("text") || res.get(0).name().equals("test")
        );
        assertEquals(
            "incorrect statistic",
            2,
            res.get(0).count()
        );
        assertEquals(
            "incorrect statistic",
            false,
            res.get(0).name().equals(res.get(1).name())
        );
        assertEquals(
            "incorrect statistic",
            true,
            res.get(1).name().equals("text") || res.get(1).name().equals("test")
        );
        assertEquals(
            "incorrect statistic",
            2,
            res.get(1).count()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest2() {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForUser(user1, 5);

        assertEquals("incorrect statistic", 3, res.size());

        assertEquals(
            "incorrect statistic",
            true,
            res.get(0).name().equals("text") || res.get(0).name().equals("test")
        );
        assertEquals(
            "incorrect statistic",
            2,
            res.get(0).count()
        );
        assertEquals(
            "incorrect statistic",
            false,
            res.get(0).name().equals(res.get(1).name())
        );
        assertEquals(
            "incorrect statistic",
            true,
            res.get(1).name().equals("text") || res.get(1).name().equals("test")
        );
        assertEquals(
            "incorrect statistic",
            2,
            res.get(1).count()
        );
        assertEquals(
            "incorrect statistic",
            "a",
            res.get(2).name()
        );
        assertEquals(
            "incorrect statistic",
            1,
            res.get(2).count()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest3() {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForUser(user3, 2);

        assertEquals("incorrect statistic", 2, res.size());

        assertEquals(
            "incorrect statistic",
            "text",
            res.get(0).name()
        );
        assertEquals(
            "incorrect statistic",
            3,
            res.get(0).count()
        );
        assertEquals(
            "incorrect statistic",
            "test",
            res.get(1).name()
        );
        assertEquals(
            "incorrect statistic",
            2,
            res.get(1).count()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest4() {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForUser(UUID.randomUUID(), 5);

        assertEquals("incorrect statistic", 0, res.size());
    }

    @Test
    public void getMostPopularWordsForTopicTest1() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForTopic(user3, "topic1", 2);

        assertEquals("incorrect statistic", 2, res.size());

        assertEquals(
            "incorrect statistic",
            "text",
            res.get(0).name()
        );
        assertEquals(
            "incorrect statistic",
            2,
            res.get(0).count()
        );
        assertEquals(
            "incorrect statistic",
            "test",
            res.get(1).name()
        );
        assertEquals(
            "incorrect statistic",
            1,
            res.get(1).count()
        );
    }

    @Test
    public void getMostPopularWordsForTopicTest2() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForTopic(user3, "topic2", 1);

        assertEquals("incorrect statistic", 1, res.size());

        assertEquals(
            "incorrect statistic",
            true,
            res.get(0).name().equals("text") || res.get(0).name().equals("test")
        );
        assertEquals(
            "Incorrect statistic",
            1,
            res.get(0).count()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest3() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForTopic(user3, "topic3", 2);

        assertEquals("incorrect statistic", 0, res.size());
    }
    @Test
    public void getMostPopularWordsForTopicTest4() {
        try {
            localStatisticService.getMostPopularWordsForTopic(user3, "topic4", 2);
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
    public void getMostPopularWordsForTopicTest5() throws RestApiException {
        try {
            localStatisticService.getMostPopularWordsForTopic(user3, "topic4", 0);
        } catch (jakarta.validation.ConstraintViolationException e) {
            assertEquals(
                "incorrect exception",
                "jakarta.validation.ConstraintViolationException: getMostPopularWordsForTopic.limit: must be greater than or equal to 1",
                e.toString()
            );
            return;
        }

        fail("an expected exception has not been dropped");
    }

    @Test
    public void getMostPopularWordsForTextTest1() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForText(
            user2, "topic1", "text211", 2
        );

        assertEquals("incorrect statistic", 2, res.size());

        assertEquals(
            "incorrect statistic",
            new WordDTO("text", 3),
            res.get(0)
        );
        assertEquals(
            "incorrect statistic",
            new WordDTO("test", 2),
            res.get(1)
        );
    }
    @Test
    public void getMostPopularWordsForTextTest2() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForText(
            user2, "topic1", "text211", 3
        );

        assertEquals("incorrect statistic", 3, res.size());

        assertEquals(
            "incorrect statistic",
            new WordDTO("text", 3),
            res.get(0)
        );
        assertEquals(
            "incorrect statistic",
            new WordDTO("test", 2),
            res.get(1)
        );
        assertEquals(
            "incorrect statistic",
            new WordDTO("a", 1),
            res.get(2)
        );
    }
    @Test
    public void getMostPopularWordsForTextTest3() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForText(
            user2, "topic1", "text211", 4
        );

        assertEquals("incorrect statistic", 3, res.size());

        assertEquals(
            "incorrect statistic",
            new WordDTO("text", 3),
            res.get(0)
        );
        assertEquals(
            "incorrect statistic",
            new WordDTO("test", 2),
            res.get(1)
        );
        assertEquals(
            "incorrect statistic",
            new WordDTO("a", 1),
            res.get(2)
        );
    }
    @Test
    public void getMostPopularWordsForTextTest4() {
        try {
            localStatisticService.getMostPopularWordsForText(
                user2, "topic9", "text211", 4
            );
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
    public void getMostPopularWordsForTextTest5() {
        try {
            localStatisticService.getMostPopularWordsForText(
                user2, "topic1", "text219", 4
            );
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a text is not found", HttpStatus.NOT_FOUND),
                e
            );
            return;
        }

        fail("an expected exception has not been dropped");
    }
    @Test
    public void getMostPopularWordsForTextTest6() throws RestApiException {
        try {
            localStatisticService.getMostPopularWordsForText(
                user2, "topic1", "text211", 0
            );
        } catch (jakarta.validation.ConstraintViolationException e) {
            assertEquals(
                "incorrect exception",
                "jakarta.validation.ConstraintViolationException: getMostPopularWordsForText.limit: must be greater than or equal to 1",
                e.toString()
            );
            return;
        }

        fail("an expected exception has not been dropped");
    }
}
