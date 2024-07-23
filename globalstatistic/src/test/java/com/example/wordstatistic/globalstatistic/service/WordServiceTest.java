package com.example.wordstatistic.globalstatistic.service;

import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.model.redis.GetPopularListResultCash;
import com.example.wordstatistic.globalstatistic.model.remote.usingHistory.UsingHistoryRecord;
import com.example.wordstatistic.globalstatistic.repository.WordRepository;
import com.example.wordstatistic.globalstatistic.repository.redis.GetPopularListResultCashRepository;
import com.example.wordstatistic.globalstatistic.util.remote.usingHistory.UsingHistoryRecordIncorrectDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WordServiceTest {
    private final WordRepository wordRepository;
    private final WordService wordService;
    @MockBean
    private final GetPopularListResultCashRepository getPopularListResultCashRepository;
    @MockBean
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    WordServiceTest(
        final WordRepository wordRepository,
        final WordService wordService,
        final GetPopularListResultCashRepository getPopularListResultCashRepository,
        final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.wordRepository = wordRepository;
        this.wordService = wordService;
        this. getPopularListResultCashRepository = getPopularListResultCashRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @LocalServerPort
    private Integer port;
    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    @BeforeEach
    void setUp() {
        wordRepository.deleteAll();
        wordRepository.save(new Word(null, "test", 1));
        wordRepository.save(new Word(null, "a", 3));
        wordRepository.save(new Word(null, "text", 1));

        when(getPopularListResultCashRepository.findByLimit(1)).thenReturn(Optional.empty());
        when(getPopularListResultCashRepository.findByLimit(2)).thenReturn(Optional.empty());
        when(getPopularListResultCashRepository.findByLimit(3)).thenReturn(Optional.empty());
    }
    @AfterEach
    void tearDown() {}

    @Test
    void getMostPopularWordsTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<GetPopularListResultCash> getPopularListResultCashCap =
            ArgumentCaptor.forClass(GetPopularListResultCash.class);
        when(getPopularListResultCashRepository.save(getPopularListResultCashCap.capture())).thenReturn(null);

        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        List<Word> r = wordService.getMostPopularWords(2);

        assertNotEquals("a list must be", null, r);
        assertEquals("an incorrect size", 2, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
        assertEquals("incorrect data", true, r.get(1).getName().equals("text") || r.get(1).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(1).getCount());

        assertEquals("incorrect cash", 1, getPopularListResultCashCap.getAllValues().size());
        assertEquals("incorrect cash", 2, getPopularListResultCashCap.getValue().getLimit());
        assertEquals("incorrect cash", 2, getPopularListResultCashCap.getValue().getResult().size());

        assertEquals(
            "incorrect cash",
            "a",
            getPopularListResultCashCap.getValue().getResult().get(0).getName()
        );
        assertEquals(
            "incorrect cash",
            3,
            getPopularListResultCashCap.getValue().getResult().get(0).getCount()
        );
        assertEquals(
            "incorrect cash",
            true,
            getPopularListResultCashCap.getValue().getResult().get(1).getName().equals("test") ||
                getPopularListResultCashCap.getValue().getResult().get(1).getName().equals("text")
        );
        assertEquals(
            "incorrect cash",
            1,
            getPopularListResultCashCap.getValue().getResult().get(1).getCount()
        );

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"getMostPopularWords\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"limit\":2,\"accepted\":2},\"longData\":{},\"floatData\":{}," +
                "\"doubleData\":{},\"stringData\":{},\"dateData\":{},\"primaryKey\":[\"limit\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void getMostPopularWordsTest2() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        when(getPopularListResultCashRepository.findByLimit(2)).thenReturn(Optional.of(
            new GetPopularListResultCash(
                10002L,
                2,
                List.of(
                    new Word(null, "a", 2),
                    new Word(null, "test", 1)
                ),
                10L
            )
        ));

        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        List<Word> r = wordService.getMostPopularWords(2);

        assertNotEquals("a list must be", null, r);
        assertEquals("an incorrect size", 2, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 2, r.get(0).getCount());
        assertEquals("incorrect data", "test", r.get(1).getName());
        assertEquals("incorrect data", 1, r.get(1).getCount());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"getMostPopularWords\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"limit\":2,\"accepted\":2},\"longData\":{},\"floatData\":{}," +
                "\"doubleData\":{},\"stringData\":{},\"dateData\":{},\"primaryKey\":[\"limit\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void getMostPopularWordsTest3() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        List<Word> r = wordService.getMostPopularWords(1);

        assertEquals("a list must be", false, r == null);
        assertEquals("an incorrect size", 1, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"getMostPopularWords\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"limit\":1,\"accepted\":1},\"longData\":{},\"floatData\":{}," +
                "\"doubleData\":{},\"stringData\":{},\"dateData\":{},\"primaryKey\":[\"limit\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void getMostPopularWordsTest4() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        List<Word> r = wordService.getMostPopularWords(3);

        assertEquals("a list must be", false, r == null);
        assertEquals("an incorrect size", 3, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
        assertEquals("incorrect data", true, r.get(1).getName().equals("text") || r.get(1).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(1).getCount());
        assertEquals("incorrect data", false, r.get(1).equals(r.get(2)));
        assertEquals("incorrect data", true, r.get(2).getName().equals("text") || r.get(2).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(2).getCount());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"getMostPopularWords\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"limit\":3,\"accepted\":3},\"longData\":{},\"floatData\":{}," +
                "\"doubleData\":{},\"stringData\":{},\"dateData\":{},\"primaryKey\":[\"limit\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void getMostPopularWordsTest5() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        List<Word> r = wordService.getMostPopularWords(5);

        assertEquals("a list must be", false, r == null);
        assertEquals("an incorrect size", 3, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
        assertEquals("incorrect data", true, r.get(1).getName().equals("text") || r.get(1).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(1).getCount());
        assertEquals("incorrect data", false, r.get(1).equals(r.get(2)));
        assertEquals("incorrect data", true, r.get(2).getName().equals("text") || r.get(2).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(2).getCount());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"getMostPopularWords\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"limit\":5,\"accepted\":3},\"longData\":{},\"floatData\":{}," +
                "\"doubleData\":{},\"stringData\":{},\"dateData\":{},\"primaryKey\":[\"limit\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void getMostPopularWordsTest6() {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        try {
            wordService.getMostPopularWords(0);
            fail("limit>=1");
        }
        catch (Exception e) {
            assertEquals(
                "limit>=1",
                "jakarta.validation.ConstraintViolationException: " +
                    "getMostPopularWords.limit: must be greater than or equal to 1",
                e.toString()
            );
            assertEquals(
                "incorrect usingHistory kafka",
                0,
                usingHistoryKafkaTopicCap.getAllValues().size()
            );
        }
    }

    @Test
    void addNewTextTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        wordService.addNewText("It's a new text for testing");

        assertEquals("an incorrect number of words", 8L, wordRepository.count());

        Optional<Word> r;
        Set<Integer> ids = new HashSet<>();

        r = wordRepository.findByName("test");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "test", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("a");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "a", r.get().getName());
        assertEquals("incorrect word data", 4, r.get().getCount());
        r = wordRepository.findByName("text");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "text", r.get().getName());
        assertEquals("incorrect word data", 2, r.get().getCount());
        r = wordRepository.findByName("it");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "it", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("s");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "s", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("new");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "new", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("for");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "for", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("testing");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "testing", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());

        assertEquals("incorrect word data", 8, ids.size());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"addNewText\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"text_length\":27},\"longData\":{\"word_count\":4}," +
                "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{}," +
                "\"primaryKey\":[\"text_length\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void addNewTextTest2() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        wordService.addNewText(" \n\r  {'IT': \"It's a new text for testing.\"}  _ ");

        assertEquals("an incorrect number of words", 8L, wordRepository.count());

        Optional<Word> r;
        Set<Integer> ids = new HashSet<>();

        r = wordRepository.findByName("test");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "test", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("a");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "a", r.get().getName());
        assertEquals("incorrect word data", 4, r.get().getCount());
        r = wordRepository.findByName("text");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "text", r.get().getName());
        assertEquals("incorrect word data", 2, r.get().getCount());
        r = wordRepository.findByName("it");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "it", r.get().getName());
        assertEquals("incorrect word data", 2, r.get().getCount());
        r = wordRepository.findByName("s");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "s", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("new");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "new", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("for");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "for", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("testing");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "testing", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());

        assertEquals("incorrect word data", 8, ids.size());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"addNewText\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"text_length\":47},\"longData\":{\"word_count\":4}," +
                "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{}," +
                "\"primaryKey\":[\"text_length\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }

    @Test
    void addNewTextTest3() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        wordService.addNewText(" \n\r  {'_': \"!@#123.\"}  _ ");

        assertEquals("an incorrect number of words", 3L, wordRepository.count());

        Optional<Word> r;
        Set<Integer> ids = new HashSet<>();

        r = wordRepository.findByName("test");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "test", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());
        r = wordRepository.findByName("a");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "a", r.get().getName());
        assertEquals("incorrect word data", 3, r.get().getCount());
        r = wordRepository.findByName("text");
        assertEquals("incorrect word data", true, r.isPresent());
        ids.add(r.get().getId());
        assertEquals("incorrect word data", "text", r.get().getName());
        assertEquals("incorrect word data", 1, r.get().getCount());

        assertEquals("incorrect word data", 3, ids.size());

        assertEquals(
            "incorrect usingHistory kafka",
            1,
            usingHistoryKafkaTopicCap.getAllValues().size()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "usingHistory",
            usingHistoryKafkaTopicCap.getValue()
        );
        assertEquals(
            "incorrect usingHistory kafka",
            "{\"serviceName\":\"globalStatistic\",\"historyTableName\":\"addNewText\",\"created\":" +
                UsingHistoryRecord.fromJSON(usingHistoryKafkaMessageCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"text_length\":25},\"longData\":{\"word_count\":0}," +
                "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{}," +
                "\"primaryKey\":[\"text_length\"]}",
            usingHistoryKafkaMessageCap.getValue()
        );
    }
    @Test
    void addNewTextTest4() {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        try {
            wordService.addNewText("");
            fail("the string must not be blank    jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank");
        }
        catch (Exception e) {
            assertEquals("the string must not be blank",
                "jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank",
                e.toString());
            assertEquals(
                "incorrect usingHistory kafka",
                0,
                usingHistoryKafkaTopicCap.getAllValues().size()
            );
        }
    }
    @Test
    void addNewTextTest5() {
        ArgumentCaptor<String> usingHistoryKafkaTopicCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usingHistoryKafkaMessageCap = ArgumentCaptor.forClass(String.class);
        when(
            kafkaTemplate.send(usingHistoryKafkaTopicCap.capture(), usingHistoryKafkaMessageCap.capture())
        ).thenReturn(null);

        try {
            wordService.addNewText(null);
            fail("the string must not be blank    jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank");
        }
        catch (Exception e) {
            assertEquals("the string must not be blank",
                "jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank",
                e.toString());
            assertEquals(
                "incorrect usingHistory kafka",
                0,
                usingHistoryKafkaTopicCap.getAllValues().size()
            );
        }
    }
}
