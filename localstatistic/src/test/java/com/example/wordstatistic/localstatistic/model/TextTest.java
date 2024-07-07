package com.example.wordstatistic.localstatistic.model;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TextListDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class TextTest {
    @Test
    public void toListDTOTest() {
        Topic topic = new Topic(1, UUID.randomUUID(), "user1", "topic1");
        Text text = new Text(1, topic, "text1", "a test text");
        assertEquals(
            "incorrect convert into a dto object",
            new TextListDTO("text1"),
            text.toListDTO()
        );
    }
    @Test
    public void toEntityTOTest() {
        Topic topic = new Topic(1, UUID.randomUUID(), "user1", "topic1");
        Text text = new Text(1, topic, "text1", "a test text");
        assertEquals(
            "incorrect convert into a dto object",
            new TextEntityDTO("topic1", "text1", "a test text"),
            text.toEntityDTO()
        );
    }

}
