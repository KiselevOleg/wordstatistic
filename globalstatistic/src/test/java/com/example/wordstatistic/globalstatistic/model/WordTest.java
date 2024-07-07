package com.example.wordstatistic.globalstatistic.model;

import com.example.wordstatistic.globalstatistic.dto.WordDTO;
import org.junit.jupiter.api.Test;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class WordTest {
    @Test
    void toDTOTest() {
        final Word w = new Word(1, "a", 3);

        WordDTO wdto = w.toDTO();

        assertEquals("incorrect transfer into a dto", "a", w.getName());
        assertEquals("incorrect transfer into a dto", 3, w.getCount());
    }
}
