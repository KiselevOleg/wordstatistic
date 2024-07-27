package com.example.wordstatistic.user.util.kafka;

import org.junit.jupiter.api.Test;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class KafkaDTOExceptionTest {
    @Test
    public void constructor() {
        final KafkaDTOException kafkaDTOException = new KafkaDTOException("text message");

        assertEquals(
            "incorrect result",
            true,
            kafkaDTOException instanceof Exception
        );
        assertEquals(
            "incorrect result",
            "text message",
            kafkaDTOException.getMessage()
        );
    }
}
