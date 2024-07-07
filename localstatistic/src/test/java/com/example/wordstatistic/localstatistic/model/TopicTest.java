package com.example.wordstatistic.localstatistic.model;

import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class TopicTest {
    @Test
    public void toDTOTest() {
        Topic topic = new Topic(1, UUID.randomUUID(), "user1", "topic1");
        assertEquals("incorrect convert into a dto object", new TopicDTO("topic1"), topic.toDTO());
    }
}
