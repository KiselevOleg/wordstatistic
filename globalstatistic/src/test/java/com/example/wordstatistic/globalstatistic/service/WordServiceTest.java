package com.example.wordstatistic.globalstatistic.service;

import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.repository.WordRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WordServiceTest {
    private final WordRepository wordRepository;
    private final WordService wordService;

    @Autowired
    WordServiceTest(final WordRepository wordRepository, final WordService wordService) {
        this.wordRepository = wordRepository;
        this.wordService = wordService;
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
    }
    @AfterEach
    void tearDown() {}

    @Test
    void getMostPopularWordsTest1() {
        List<Word> r = wordService.getMostPopularWords(2);

        assertNotEquals("a list must be", null, r);
        assertEquals("an incorrect size", 2, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
        assertEquals("incorrect data", true, r.get(1).getName().equals("text") || r.get(1).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(1).getCount());
    }
    @Test
    void getMostPopularWordsTest2() {
        List<Word> r = wordService.getMostPopularWords(1);

        assertNotEquals("a list must be", null, r);
        assertEquals("an incorrect size", 1, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
    }
    @Test
    void getMostPopularWordsTest3() {
        List<Word> r = wordService.getMostPopularWords(3);

        assertNotEquals("a list must be", null, r);
        assertEquals("an incorrect size", 3, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
        assertEquals("incorrect data", true, r.get(1).getName().equals("text") || r.get(1).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(1).getCount());
        assertEquals("incorrect data", false, r.get(1).equals(r.get(2)));
        assertEquals("incorrect data", true, r.get(2).getName().equals("text") || r.get(2).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(2).getCount());
    }
    @Test
    void getMostPopularWordsTest4() {
        List<Word> r = wordService.getMostPopularWords(5);

        assertNotEquals("a list must be", null, r);
        assertEquals("an incorrect size", 3, r.size());

        assertEquals("incorrect data", "a", r.get(0).getName());
        assertEquals("incorrect data", 3, r.get(0).getCount());
        assertEquals("incorrect data", true, r.get(1).getName().equals("text") || r.get(1).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(1).getCount());
        assertEquals("incorrect data", false, r.get(1).equals(r.get(2)));
        assertEquals("incorrect data", true, r.get(2).getName().equals("text") || r.get(2).getName().equals("test"));
        assertEquals("incorrect data", 1, r.get(2).getCount());
    }
    @Test
    void getMostPopularWordsTest5() {
        try {
            wordService.getMostPopularWords(0);
            fail("limit>=1");
        }
        catch (Exception e) {
            assertEquals("limit>=1", "jakarta.validation.ConstraintViolationException: getMostPopularWords.limit: must be greater than or equal to 1", e.toString());
        }
    }

    @Test
    void getLocationObjectByNameIntegrationTest1() {
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
    }
    @Test
    void getLocationObjectByNameIntegrationTest2() {
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
    }

    @Test
    void getLocationObjectByNameIntegrationTest3() {
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
    }
    @Test
    void getLocationObjectByNameIntegrationTest4() {
        try {
            wordService.addNewText("");
            fail("the string must not be blank    jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank");
        }
        catch (Exception e) {
            assertEquals("the string must not be blank",
                "jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank",
                e.toString());
        }

        try {
            wordService.addNewText(null);
            fail("the string must not be blank    jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank");
        }
        catch (Exception e) {
            assertEquals("the string must not be blank",
                "jakarta.validation.ConstraintViolationException: addNewText.text: must not be blank",
                e.toString());
        }
    }
}
