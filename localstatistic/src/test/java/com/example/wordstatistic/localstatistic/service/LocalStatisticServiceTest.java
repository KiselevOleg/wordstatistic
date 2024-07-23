package com.example.wordstatistic.localstatistic.service;

import com.example.wordstatistic.localstatistic.client.UsingHistoryService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalStatisticServiceTest {
    private final TextRepository textRepository;
    private final TopicRepository topicRepository;
    private final LocalStatisticService localStatisticService;

    @MockBean
    private final UsingHistoryService usingHistory;

    @MockBean
    private final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository;
    @MockBean
    private final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository;
    @MockBean
    private final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository;

    private UUID user1, user2, user3;

    private ArgumentCaptor<GetMostPopularWordsListForUserCash> getMostPopularWordsListForUserCashSaveCap;
    private ArgumentCaptor<GetMostPopularWordsListForTopicCash> getMostPopularWordsListForTopicCashSaveCap;
    private ArgumentCaptor<GetMostPopularWordsListForTextCash> getMostPopularWordsListForTextCashSaveCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForUserCashFindUserIdCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForTopicCashFindUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTopicCashFindTopicIdCap;

    private ArgumentCaptor<UUID> getMostPopularWordsListForTextCashFindUserIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashFindTopicIdCap;
    private ArgumentCaptor<Integer> getMostPopularWordsListForTextCashFindTextIdCap;

    private ArgumentCaptor<Long> getMostPopularWordsListForUserCashDeleteIdCap;
    private ArgumentCaptor<Long> getMostPopularWordsListForTopicCashDeleteIdCap;
    private ArgumentCaptor<Long> getMostPopularWordsListForTextCashDeleteIdCap;

    @Autowired
    public LocalStatisticServiceTest(
        final TextRepository textRepository,
        final TopicRepository topicRepository,
        final LocalStatisticService localStatisticService,
        final UsingHistoryService usingHistory,
        final GetMostPopularWordsListForUserCashRepository getMostPopularWordsListForUserCashRepository,
        final GetMostPopularWordsListForTopicCashRepository getMostPopularWordsListForTopicCashRepository,
        final GetMostPopularWordsListForTextCashRepository getMostPopularWordsListForTextCashRepository
    ) {
        this.textRepository = textRepository;
        this.topicRepository = topicRepository;
        this.localStatisticService = localStatisticService;
        this.usingHistory = usingHistory;
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

        getMostPopularWordsListForUserCashSaveCap = ArgumentCaptor.forClass(GetMostPopularWordsListForUserCash.class);
        //when(getMostPopularWordsListForUserCashRepository.save(getMostPopularWordsListForUserCashSaveCap.capture()))
        //    .thenReturn(null);
        getMostPopularWordsListForTopicCashSaveCap = ArgumentCaptor.forClass(GetMostPopularWordsListForTopicCash.class);
        //when(getMostPopularWordsListForTopicCashRepository.save(getMostPopularWordsListForTopicCashSaveCap.capture()))
        //    .thenReturn(null);
        getMostPopularWordsListForTextCashSaveCap = ArgumentCaptor.forClass(GetMostPopularWordsListForTextCash.class);
        //when(getMostPopularWordsListForTextCashRepository.save(getMostPopularWordsListForTextCashSaveCap.capture()))
        //    .thenReturn(null);

        getMostPopularWordsListForUserCashFindUserIdCap = ArgumentCaptor.forClass(UUID.class);
        //when(getMostPopularWordsListForUserCashRepository.findByUserId(
        //    getMostPopularWordsListForUserCashFindUserIdCap.capture()
        //)).thenReturn(Optional.empty());
        getMostPopularWordsListForTopicCashFindUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForTopicCashFindTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        //when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
        //    getMostPopularWordsListForTopicCashFindUserIdCap.capture(),
        //    getMostPopularWordsListForTopicCashFindTopicIdCap.capture()
        //)).thenReturn(Optional.empty());
        getMostPopularWordsListForTextCashFindUserIdCap = ArgumentCaptor.forClass(UUID.class);
        getMostPopularWordsListForTextCashFindTopicIdCap = ArgumentCaptor.forClass(Integer.class);
        getMostPopularWordsListForTextCashFindTextIdCap = ArgumentCaptor.forClass(Integer.class);
        //when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
        //    getMostPopularWordsListForTextCashFindUserIdCap.capture(),
        //    getMostPopularWordsListForTextCashFindTopicIdCap.capture(),
        //    getMostPopularWordsListForTextCashFindTextIdCap.capture()
        //)).thenReturn(Optional.empty());

        getMostPopularWordsListForUserCashDeleteIdCap = ArgumentCaptor.forClass(Long.class);
        //doNothing().when(getMostPopularWordsListForUserCashRepository).deleteByUserId(
        //    getMostPopularWordsListForUserCashDeleteUserIdCap.capture()
        //);
        getMostPopularWordsListForTopicCashDeleteIdCap = ArgumentCaptor.forClass(Long.class);
        //doNothing().when(getMostPopularWordsListForTopicCashRepository).deleteByUserIdAndTopicId(
        //    getMostPopularWordsListForTopicCashDeleteUserIdCap.capture(),
        //    getMostPopularWordsListForTopicCashDeleteTopicIdCap.capture()
        //);
        getMostPopularWordsListForTextCashDeleteIdCap = ArgumentCaptor.forClass(Long.class);
        //doNothing().when(getMostPopularWordsListForTextCashRepository).deleteByUserIdAndTopicIdAndTextId(
        //    getMostPopularWordsListForTextCashDeleteUserIdCap.capture(),
        //    getMostPopularWordsListForTextCashDeleteTopicIdCap.capture(),
        //    getMostPopularWordsListForTextCashDeleteTextIdCap.capture()
        //);
    }
    @AfterEach
    void tearDown() { }

    @Test
    public void getMostPopularWordsForUserTest1() {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdMethod = "getMostPopularWordsListForUserCashRepository.findByUserId";
        final String deleteByIdMethod = "getMostPopularWordsListForUserCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForUserCashRepository.save";
        when(getMostPopularWordsListForUserCashRepository.findByUserId(getMostPopularWordsListForUserCashFindUserIdCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(findByUserIdMethod);
                return Optional.empty();
            });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForUserCashRepository).deleteById(getMostPopularWordsListForUserCashDeleteIdCap.capture());
        when(getMostPopularWordsListForUserCashRepository.save(getMostPopularWordsListForUserCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 2, callMethods.size());
        assertEquals("incorrect cash", findByUserIdMethod, callMethods.get(0));
        assertEquals("incorrect cash", saveMethod, callMethods.get(1));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user1,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest2() {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdMethod = "getMostPopularWordsListForUserCashRepository.findByUserId";
        final String deleteByIdMethod = "getMostPopularWordsListForUserCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForUserCashRepository.save";
        when(getMostPopularWordsListForUserCashRepository.findByUserId(
            getMostPopularWordsListForUserCashFindUserIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdMethod);
            return Optional.of(new GetMostPopularWordsListForUserCash(
                10L,
                1,
                user1,
                List.of(
                    new WordDTO("text", 1)
                ),
                null
            ));
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForUserCashRepository).deleteById(getMostPopularWordsListForUserCashDeleteIdCap.capture());
        when(getMostPopularWordsListForUserCashRepository.save(getMostPopularWordsListForUserCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 3, callMethods.size());
        assertEquals("incorrect cash", findByUserIdMethod, callMethods.get(0));
        assertEquals("incorrect cash", deleteByIdMethod, callMethods.get(1));
        assertEquals("incorrect cash", saveMethod, callMethods.get(2));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            10L,
            getMostPopularWordsListForUserCashDeleteIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user1,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest3() {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdMethod = "getMostPopularWordsListForUserCashRepository.findByUserId";
        final String deleteByIdMethod = "getMostPopularWordsListForUserCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForUserCashRepository.save";
        when(getMostPopularWordsListForUserCashRepository.findByUserId(
            getMostPopularWordsListForUserCashFindUserIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdMethod);
            return Optional.of(new GetMostPopularWordsListForUserCash(
                5L,
                3,
                user1,
                List.of(
                    new WordDTO("text", 1),
                    new WordDTO("test", 1),
                    new WordDTO("a", 1)
                ),
                null
            ));
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForUserCashRepository).deleteById(getMostPopularWordsListForUserCashDeleteIdCap.capture());
        when(getMostPopularWordsListForUserCashRepository.save(getMostPopularWordsListForUserCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 1, callMethods.size());
        assertEquals("incorrect cash", findByUserIdMethod, callMethods.get(0));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user1,
            getMostPopularWordsListForUserCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user1,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest4() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 3,
                "limit", 5,
                "userId", usingHistoryParametersCap.getValue().get("userId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user1,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest5() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForUserTest6() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        final UUID userNew = UUID.randomUUID();
        List<WordDTO> res = localStatisticService.getMostPopularWordsForUser(userNew, 5);

        assertEquals("incorrect statistic", 0, res.size());

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForUser",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 0,
                "limit", 5,
                "userId", usingHistoryParametersCap.getValue().get("userId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            userNew,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }

    @Test
    public void getMostPopularWordsForTopicTest1() throws RestApiException {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdAndTopicIdMethod = "getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId";
        final String deleteByIdMethod = "getMostPopularWordsListForTopicCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForTopicCashRepository.save";
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            getMostPopularWordsListForTopicCashFindUserIdCap.capture(),
            getMostPopularWordsListForTopicCashFindTopicIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdAndTopicIdMethod);
            return Optional.empty();
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashDeleteIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.save(getMostPopularWordsListForTopicCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 2, callMethods.size());
        assertEquals("incorrect cash", findByUserIdAndTopicIdMethod, callMethods.get(0));
        assertEquals("incorrect cash", saveMethod, callMethods.get(1));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashFindTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );


        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest2() throws RestApiException {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdAndTopicIdMethod = "getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId";
        final String deleteByIdMethod = "getMostPopularWordsListForTopicCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForTopicCashRepository.save";
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            getMostPopularWordsListForTopicCashFindUserIdCap.capture(),
            getMostPopularWordsListForTopicCashFindTopicIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdAndTopicIdMethod);
            return Optional.of(new GetMostPopularWordsListForTopicCash(
                2L,
                1,
                user1,
                topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
                List.of(
                    new WordDTO("text", 2)
                ),
                null
            ));
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashDeleteIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.save(getMostPopularWordsListForTopicCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 3, callMethods.size());
        assertEquals("incorrect cash", findByUserIdAndTopicIdMethod, callMethods.get(0));
        assertEquals("incorrect cash", deleteByIdMethod, callMethods.get(1));
        assertEquals("incorrect cash", saveMethod, callMethods.get(2));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashFindTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            2L,
            getMostPopularWordsListForTopicCashDeleteIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest3() throws RestApiException {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdAndTopicIdMethod = "getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId";
        final String deleteByIdMethod = "getMostPopularWordsListForTopicCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForTopicCashRepository.save";
        when(getMostPopularWordsListForTopicCashRepository.findByUserIdAndTopicId(
            getMostPopularWordsListForTopicCashFindUserIdCap.capture(),
            getMostPopularWordsListForTopicCashFindTopicIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdAndTopicIdMethod);
            return Optional.of(new GetMostPopularWordsListForTopicCash(
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
            ));
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForTopicCashRepository).deleteById(
            getMostPopularWordsListForTopicCashDeleteIdCap.capture()
        );
        when(getMostPopularWordsListForTopicCashRepository.save(getMostPopularWordsListForTopicCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 1, callMethods.size());
        assertEquals("incorrect cash", findByUserIdAndTopicIdMethod, callMethods.get(0));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user3,
            getMostPopularWordsListForTopicCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            getMostPopularWordsListForTopicCashFindTopicIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user3, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest4() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 1,
                "limit", 1,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic2",
                "topicId", usingHistoryParametersCap.getValue().get("topicId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user3, "topic2").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest5() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        List<WordDTO> res = localStatisticService.getMostPopularWordsForTopic(user3, "topic3", 2);

        assertEquals("incorrect statistic", 0, res.size());

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForTopic",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 0,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic3",
                "topicId", usingHistoryParametersCap.getValue().get("topicId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user3,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user3, "topic3").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest6() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localStatisticService.getMostPopularWordsForTopic(user3, "topic4", 2);
        } catch (RestApiException e) {
            assertEquals(
                "incorrect exception",
                new RestApiException("a topic is not found", HttpStatus.NOT_FOUND),
                e
            );

            assertEquals(
                "incorrect history message",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect history message",
                "getMostPopularWordsForTopic_topicNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect history message",
                Map.of(
                    "userId", usingHistoryParametersCap.getValue().get("userId"),
                    "limit", 2,
                    "topicName", "topic4"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect history message",
                user3,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
            );
            assertEquals(
                "incorrect history message",
                Set.of(
                    "topicName"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }

        fail("an expected exception has not been dropped");
    }
    @Test
    public void getMostPopularWordsForTopicTest7() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

        try {
            localStatisticService.getMostPopularWordsForTopic(user3, "topic4", 0);
        } catch (jakarta.validation.ConstraintViolationException e) {
            assertEquals(
                "incorrect exception",
                "jakarta.validation.ConstraintViolationException: getMostPopularWordsForTopic.limit: must be greater than or equal to 1",
                e.toString()
            );

            assertEquals(
                "incorrect history message",
                0,
                usingHistoryOperationNameCap.getAllValues().size()
            );

            return;
        }

        fail("an expected exception has not been dropped");
    }

    @Test
    public void getMostPopularWordsForTextTest1() throws RestApiException {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdAndTopicIdAndTextIdMethod = "getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId";
        final String deleteByIdMethod = "getMostPopularWordsListForTextCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForTextCashRepository.save";
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            getMostPopularWordsListForTextCashFindUserIdCap.capture(),
            getMostPopularWordsListForTextCashFindTopicIdCap.capture(),
            getMostPopularWordsListForTextCashFindTextIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdAndTopicIdAndTextIdMethod);
            return Optional.empty();
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashDeleteIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.save(getMostPopularWordsListForTextCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });

        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 2, callMethods.size());
        assertEquals("incorrect cash", findByUserIdAndTopicIdAndTextIdMethod, callMethods.get(0));
        assertEquals("incorrect cash", saveMethod, callMethods.get(1));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashFindTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashFindTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId"),
                "textName", "text211",
                "textId", usingHistoryParametersCap.getValue().get("textId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user2,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            textRepository.findByTopicAndName(topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211").get().getId(),
            usingHistoryParametersCap.getValue().get("textId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTextTest2() throws RestApiException {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdAndTopicIdAndTextIdMethod = "getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId";
        final String deleteByIdMethod = "getMostPopularWordsListForTextCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForTextCashRepository.save";
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            getMostPopularWordsListForTextCashFindUserIdCap.capture(),
            getMostPopularWordsListForTextCashFindTopicIdCap.capture(),
            getMostPopularWordsListForTextCashFindTextIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdAndTopicIdAndTextIdMethod);
            return Optional.of(new GetMostPopularWordsListForTextCash(
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
            ));
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashDeleteIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.save(getMostPopularWordsListForTextCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 3, callMethods.size());
        assertEquals("incorrect cash", findByUserIdAndTopicIdAndTextIdMethod, callMethods.get(0));
        assertEquals("incorrect cash", deleteByIdMethod, callMethods.get(1));
        assertEquals("incorrect cash", saveMethod, callMethods.get(2));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashFindTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashFindTextIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            1L,
            getMostPopularWordsListForTextCashDeleteIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId"),
                "textName", "text211",
                "textId", usingHistoryParametersCap.getValue().get("textId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user2,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            textRepository.findByTopicAndName(topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211").get().getId(),
            usingHistoryParametersCap.getValue().get("textId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }

    @Test
    public void getMostPopularWordsForTextTest3() throws RestApiException {
        final List<String> callMethods = new ArrayList<>();
        final String findByUserIdAndTopicIdAndTextIdMethod = "getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId";
        final String deleteByIdMethod = "getMostPopularWordsListForTextCashRepository.deleteById";
        final String saveMethod = "getMostPopularWordsListForTextCashRepository.save";
        when(getMostPopularWordsListForTextCashRepository.findByUserIdAndTopicIdAndTextId(
            getMostPopularWordsListForTextCashFindUserIdCap.capture(),
            getMostPopularWordsListForTextCashFindTopicIdCap.capture(),
            getMostPopularWordsListForTextCashFindTextIdCap.capture()
        )).thenAnswer((e) -> {
            callMethods.add(findByUserIdAndTopicIdAndTextIdMethod);
            return Optional.of(new GetMostPopularWordsListForTextCash(
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
            ));
        });
        doAnswer((e) -> {
            callMethods.add(deleteByIdMethod);
            return null;
        }).when(getMostPopularWordsListForTextCashRepository).deleteById(
            getMostPopularWordsListForTextCashDeleteIdCap.capture()
        );
        when(getMostPopularWordsListForTextCashRepository.save(getMostPopularWordsListForTextCashSaveCap.capture()))
            .thenAnswer((e) -> {
                callMethods.add(saveMethod);
                return null;
            });
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals("incorrect cash", 1, callMethods.size());
        assertEquals("incorrect cash", findByUserIdAndTopicIdAndTextIdMethod, callMethods.get(0));
        assertEquals(
            "incorrect cash",
            1,
            getMostPopularWordsListForTextCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            user2,
            getMostPopularWordsListForTextCashFindUserIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            getMostPopularWordsListForTextCashFindTopicIdCap.getValue()
        );
        assertEquals(
            "incorrect cash",
            textRepository.findByTopicAndName(
                topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211"
            ).get().getId(),
            getMostPopularWordsListForTextCashFindTextIdCap.getValue()
        );

        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashFindUserIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForUserCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTopicCashDeleteIdCap.getAllValues().size()
        );
        assertEquals(
            "incorrect cash",
            0,
            getMostPopularWordsListForTextCashDeleteIdCap.getAllValues().size()
        );

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 2,
                "limit", 2,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId"),
                "textName", "text211",
                "textId", usingHistoryParametersCap.getValue().get("textId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user2,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            textRepository.findByTopicAndName(topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211").get().getId(),
            usingHistoryParametersCap.getValue().get("textId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTextTest4() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );
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

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 3,
                "limit", 3,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId"),
                "textName", "text211",
                "textId", usingHistoryParametersCap.getValue().get("textId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user2,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            textRepository.findByTopicAndName(topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211").get().getId(),
            usingHistoryParametersCap.getValue().get("textId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTextTest5() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

        assertEquals(
            "incorrect history message",
            1,
            usingHistoryOperationNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect history message",
            "getMostPopularWordsForText",
            usingHistoryOperationNameCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            Map.of(
                "accepted", 3,
                "limit", 4,
                "userId", usingHistoryParametersCap.getValue().get("userId"),
                "topicName", "topic1",
                "topicId", usingHistoryParametersCap.getValue().get("topicId"),
                "textName", "text211",
                "textId", usingHistoryParametersCap.getValue().get("textId")
            ),
            usingHistoryParametersCap.getValue()
        );
        assertEquals(
            "incorrect history message",
            user2,
            UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
        );
        assertEquals(
            "incorrect history message",
            topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
            usingHistoryParametersCap.getValue().get("topicId")
        );
        assertEquals(
            "incorrect history message",
            textRepository.findByTopicAndName(topicRepository.findByUserIdAndName(user2, "topic1").get(), "text211").get().getId(),
            usingHistoryParametersCap.getValue().get("textId")
        );
        assertEquals(
            "incorrect history message",
            Set.of(
                "limit"
            ),
            usingHistoryPrimaryKeyCap.getValue()
        );
    }
    @Test
    public void getMostPopularWordsForTextTest6() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

            assertEquals(
                "incorrect history message",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect history message",
                "getMostPopularWordsForText_TopicNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect history message",
                Map.of(
                    "limit", 4,
                    "userId", usingHistoryParametersCap.getValue().get("userId"),
                    "topicName", "topic9",
                    "textName", "text211"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect history message",
                user2,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
            );
            assertEquals(
                "incorrect history message",
                Set.of(
                    "topicName", "textName"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );
            return;
        }

        fail("an expected exception has not been dropped");
    }
    @Test
    public void getMostPopularWordsForTextTest7() {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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

            assertEquals(
                "incorrect history message",
                1,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            assertEquals(
                "incorrect history message",
                "getMostPopularWordsForText_TextNotFoundError",
                usingHistoryOperationNameCap.getValue()
            );
            assertEquals(
                "incorrect history message",
                Map.of(
                    "limit", 4,
                    "userId", usingHistoryParametersCap.getValue().get("userId"),
                    "topicName", "topic1",
                    "topicId", usingHistoryParametersCap.getValue().get("topicId"),
                    "textName", "text219"
                ),
                usingHistoryParametersCap.getValue()
            );
            assertEquals(
                "incorrect history message",
                user2,
                UUID.fromString((String) usingHistoryParametersCap.getValue().get("userId"))
            );
            assertEquals(
                "incorrect history message",
                topicRepository.findByUserIdAndName(user2, "topic1").get().getId(),
                usingHistoryParametersCap.getValue().get("topicId")
            );
            assertEquals(
                "incorrect history message",
                Set.of(
                    "topicName", "textName"
                ),
                usingHistoryPrimaryKeyCap.getValue()
            );

            return;
        }

        fail("an expected exception has not been dropped");
    }
    @Test
    public void getMostPopularWordsForTextTest8() throws RestApiException {
        ArgumentCaptor<String> usingHistoryOperationNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> usingHistoryParametersCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Set<String>> usingHistoryPrimaryKeyCap = ArgumentCaptor.forClass(Set.class);
        doNothing().when(usingHistory).sendMessage(
            usingHistoryOperationNameCap.capture(),
            usingHistoryParametersCap.capture(),
            usingHistoryPrimaryKeyCap.capture()
        );

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
            assertEquals(
                "incorrect history message",
                0,
                usingHistoryOperationNameCap.getAllValues().size()
            );
            return;
        }

        fail("an expected exception has not been dropped");
    }
}
