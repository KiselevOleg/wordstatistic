package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.dto.WordDTO;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalStatisticServiceTest {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;
    private final LocalStatisticService localStatisticService;

    @MockBean
    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    @MockBean
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    @MockBean
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;

    private UUID user1, user2, user3;

    private ArgumentCaptor<UUID> getMostPopularWordsListForUserCashSaveUserIdCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForTopicCashSaveUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTopicCashSaveTopicIdCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForTextCashSaveUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashSaveTopicIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashSaveTextIdCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForUserCashDeleteUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForUserCashDeleteLimitCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForTopicCashDeleteUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTopicCashDeleteTopicIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTopicCashDeleteLimitCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForTextCashDeleteUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashDeleteTopicIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashDeleteTextIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashDeleteLimitCap;

    @Autowired
    public LocalStatisticServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalStatisticService localStatisticService,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localStatisticService = localStatisticService;
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

        getMostPopularWordsListForUserCashSaveUserIdCap = ArgumentCaptor.forClass(UUID.class);
        when(getMostPopularWordsListForUserCashRepository.findByUserId(
            getMostPopularWordsListForUserCashSaveUserIdCap.capture()
        )).thenReturn(Optional.empty());
        getMostPopularWordsListForTopicCashSaveUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForTopicCashSaveTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            getMostPopularWordsListForTopicCashSaveUserIdCap.capture(),
            getMostPopularWordsListForTopicCashSaveTopicIdCap.capture()
        )).thenReturn(Optional.empty());
        getMostPopularWordsListForTextCashSaveUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForTextCashSaveTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        getMostPopularWordsListForTextCashSaveTextIdCap = ArgumentCaptor.forClass(Integer.class);
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            getMostPopularWordsListForTextCashSaveUserIdCap.capture(),
            getMostPopularWordsListForTextCashSaveTopicIdCap.capture(),
            getMostPopularWordsListForTextCashSaveTextIdCap.capture()
        )).thenReturn(Optional.empty());

        getMostPopularWordsListForUserCashDeleteUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForUserCashDeleteLimitCap = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(getMostPopularWordsListForUserCashRepository).deleteByUserIdAndLimitLessThan(
            getMostPopularWordsListForUserCashDeleteUserIdCap.capture(),
            getMostPopularWordsListForUserCashDeleteLimitCap.capture()
        );
        getMostPopularWordsListForTopicCashDeleteUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForTopicCashDeleteTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        getMostPopularWordsListForTopicCashDeleteLimitCap = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteByUserIdAndTopicIdLimitLessThan(
            getMostPopularWordsListForTopicCashDeleteUserIdCap.capture(),
            getMostPopularWordsListForTopicCashDeleteTopicIdCap.capture(),
            getMostPopularWordsListForTopicCashDeleteLimitCap.capture()
        );
        getMostPopularWordsListForTextCashDeleteUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForTextCashDeleteTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        getMostPopularWordsListForTextCashDeleteTextIdCap = ArgumentCaptor.forClass(Integer.class);
        getMostPopularWordsListForTextCashDeleteLimitCap = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(getMostPopularWordsListForTextCashRepository).deleteByUserIdAndTopicIdAndTextIdAndLimitLessThan(
            getMostPopularWordsListForTextCashDeleteUserIdCap.capture(),
            getMostPopularWordsListForTextCashDeleteTopicIdCap.capture(),
            getMostPopularWordsListForTextCashDeleteTextIdCap.capture(),
            getMostPopularWordsListForTextCashDeleteLimitCap.capture()
        );
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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            2,
            getMostPopularWordsListForUserCashDeleteLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest2() {
        when(getMostPopularWordsListForUserCashRepository.findByUserId(
            user1
        )).thenReturn(Optional.of(new GetMostPopularWordsListForUserCash(
            1L,
            1,
            user1,
            List.of(
                new WordDTO("text", 1)
            ),
            null
        )));

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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            2,
            getMostPopularWordsListForUserCashDeleteLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest3() {
        when(getMostPopularWordsListForUserCashRepository.findByUserId(
            user1
        )).thenReturn(Optional.of(new GetMostPopularWordsListForUserCash(
            1L,
            3,
            user1,
            List.of(
                new WordDTO("text", 1),
                new WordDTO("test", 1),
                new WordDTO("a", 1)
            ),
            null
        )));

        List<WordDTO> res = localStatisticService.getMostPopularWordsForUser(user1, 2);

        assertEquals("incorrect statistic", 2, res.size());

        assertEquals(
            "incorrect statistic",
            "text",
            res.get(0).name()
        );
        assertEquals(
            "incorrect statistic",
            1,
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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest4() {
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
    public void getMostPopularWordsForUserTest5() {
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
    public void getMostPopularWordsForUserTest6() {
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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashSaveTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashDeleteTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            2,
            getMostPopularWordsListForTopicCashDeleteLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest2() throws RestApiException {
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(Optional.of(new GetMostPopularWordsListForTopicCash(
            1L,
            1,
            user1,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            List.of(
                new WordDTO("text", 2)
            ),
            null
        )));
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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashSaveTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashDeleteTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            2,
            getMostPopularWordsListForTopicCashDeleteLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest3() throws RestApiException {
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            user3, topicRepository.findByUserIdAndName(user3, "topic1").get().getId()
        )).thenReturn(Optional.of(new GetMostPopularWordsListForTopicCash(
            1L,
            3,
            user1,
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            List.of(
                new WordDTO("text", 2),
                new WordDTO("a", 1),
                new WordDTO("testing", 1)
            ),
            null
        )));

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
            "a",
            res.get(1).name()
        );
        assertEquals(
            "incorrect statistic",
            1,
            res.get(1).count()
        );

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashSaveTopicIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest4() throws RestApiException {
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
    public void getMostPopularWordsForTopicTest5() throws RestApiException {
        List<WordDTO> res = localStatisticService.getMostPopularWordsForTopic(user3, "topic3", 2);

        assertEquals("incorrect statistic", 0, res.size());
    }
    @Test
    public void getMostPopularWordsForTopicTest6() {
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
    public void getMostPopularWordsForTopicTest7() throws RestApiException {
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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashSaveTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashSaveTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashDeleteTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashDeleteTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            2,
            getMostPopularWordsListForTextCashDeleteLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForTextTest2() throws RestApiException {
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user2,
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId()
        )).thenReturn(Optional.of(new GetMostPopularWordsListForTextCash(
            1L,
            1,
            user2,
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            List.of(
                new WordDTO("text", 2)
            ),
            null
        )));

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

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashSaveTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashSaveTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashDeleteTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashDeleteTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            2,
            getMostPopularWordsListForTextCashDeleteLimitCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
    }

    @Test
    public void getMostPopularWordsForTextTest3() throws RestApiException {
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            user2,
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId()
        )).thenReturn(Optional.of(new GetMostPopularWordsListForTextCash(
            1L,
            3,
            user2,
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            List.of(
                new WordDTO("text", 2),
                new WordDTO("test", 2),
                new WordDTO("a", 1)
            ),
            null
        )));

        List<WordDTO> res = localStatisticService.getMostPopularWordsForText(
            user2, "topic1", "text211", 2
        );

        assertEquals("incorrect statistic", 2, res.size());

        assertEquals(
            "incorrect statistic",
            new WordDTO("text", 2),
            res.get(0)
        );
        assertEquals(
            "incorrect statistic",
            new WordDTO("test", 2),
            res.get(1)
        );

        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashSaveUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashSaveTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashSaveTextIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashSaveUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteUserIdCap.getAllValues().size()
        );
    }
    @Test
    public void getMostPopularWordsForTextTest4() throws RestApiException {
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
    public void getMostPopularWordsForTextTest5() throws RestApiException {
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
    public void getMostPopularWordsForTextTest6() {
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
    public void getMostPopularWordsForTextTest7() {
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
    public void getMostPopularWordsForTextTest8() throws RestApiException {
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
