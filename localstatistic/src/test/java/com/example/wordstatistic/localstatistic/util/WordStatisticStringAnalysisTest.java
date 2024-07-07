package com.example.wordstatistic.localstatistic.util;

import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static com.example.wordstatistic.localstatistic.util.WordStatisticStringAnalysis.getAllWords;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class WordStatisticStringAnalysisTest {
    @BeforeEach
    void setUp() { }

    @AfterEach
    void tearDown() { }

    @BeforeAll
    static void beforeAll() { }

    @AfterAll
    static void afterAll() { }

    @Test
    public void getAllWordsTest1() {
        final String text = "it's a test text for testing in this test";
        final Map<String,Integer> res = new HashMap<>();
        res.put("it",1);
        res.put("s",1);
        res.put("a",1);
        res.put("test",2);
        res.put("text",1);
        res.put("for",1);
        res.put("testing",1);
        res.put("in",1);
        res.put("this",1);
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, getAllWords(text));
    }
    @Test
    public void getAllWordsTest2() {
        final String text = " \n\r  {'IT': \"It's a test text for testing in this test.\"}  _ ";
        final Map<String,Integer> res = new HashMap<>();
        res.put("it",2);
        res.put("s",1);
        res.put("a",1);
        res.put("test",2);
        res.put("text",1);
        res.put("for",1);
        res.put("testing",1);
        res.put("in",1);
        res.put("this",1);
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, getAllWords(text));
    }
    @Test
    public void getAllWordsTest3() {
        final String text = " \n\r  {'_': \"!@#123.\"}  _ ";
        final Map<String,Integer> res = new HashMap<>();
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, getAllWords(text));
    }
    @Test
    public void getAllWordsTest4() {
        final String text = "";
        final Map<String,Integer> res = new HashMap<>();
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, getAllWords(text));
    }
    @Test
    public void getAllWordsTest5() {
        final String text = null;
        final Map<String,Integer> res = new HashMap<>();
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, getAllWords(text));
    }

    @Test
    public void getAllWordsTest6() {
        final Map<String,Integer> input = new HashMap<>();
        input.put("it",1);
        input.put("s",1);
        input.put("a",1);
        input.put("test",2);
        input.put("text",1);
        input.put("for",1);
        input.put("testing",1);
        input.put("in",1);
        input.put("this",1);

        final String text = "a test test text";
        final Map<String,Integer> res = new HashMap<>();
        res.put("it",1);
        res.put("s",1);
        res.put("a",2);
        res.put("test",4);
        res.put("text",2);
        res.put("for",1);
        res.put("testing",1);
        res.put("in",1);
        res.put("this",1);
        getAllWords(input, text);
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, input);
    }
    @Test
    public void getAllWordsTest7() {
        final Map<String,Integer> input = new HashMap<>();
        input.put("it",1);
        input.put("s",1);
        input.put("a",1);
        input.put("test",2);
        input.put("text",1);
        input.put("for",1);
        input.put("testing",1);
        input.put("in",1);
        input.put("this",1);

        final String text = "{}^&*}";
        final Map<String,Integer> res = new HashMap<>();
        res.put("it",1);
        res.put("s",1);
        res.put("a",1);
        res.put("test",2);
        res.put("text",1);
        res.put("for",1);
        res.put("testing",1);
        res.put("in",1);
        res.put("this",1);
        getAllWords(input, text);
        assertEquals("WordStatisticStringAnalysis.getAllWords test fail",
            res, input);
    }
}
