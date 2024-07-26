package com.example.wordstatistic.localstatistic.dto.kafka;

import com.example.wordstatistic.localstatistic.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

class ChangeUsernameDTOTest {
    @Test
    public void toJSONTest1() throws KafkaDTOException, JsonProcessingException {
        final UUID user = UUID.randomUUID();
        final ChangeUsernameDTO dto = new ChangeUsernameDTO(user, "testName");

        assertEquals(
            "incorrect result",
            "{\"userId\":\"" + user + "\",\"newUsername\":\"testName\"}",
            ChangeUsernameDTO.toJSON(dto)
        );
    }
    @Test
    public void toJSONTest2() throws KafkaDTOException, JsonProcessingException {
        final UUID user = UUID.randomUUID();
        final ChangeUsernameDTO dto = new ChangeUsernameDTO(user, "");

        try {
            ChangeUsernameDTO.toJSON(dto);
        } catch(KafkaDTOException e) {
            assertEquals("incorrect result", "a new username is blank" ,e.getMessage());
            return;
        }

        fail("incorrect result");
    }
    @Test
    public void fromJSONTest1() throws KafkaDTOException, JsonProcessingException {
        final String json = "{\"userId\":\"1ec3454c-ecad-467f-9c59-7ad954c108f7\",\"newUsername\":\"testName\"}";
        final ChangeUsernameDTO dto = ChangeUsernameDTO.fromJSON(json);

        assertEquals(
            "incorrect result",
            "1ec3454c-ecad-467f-9c59-7ad954c108f7",
            dto.userId().toString()
        );
        assertEquals(
            "incorrect result",
            "testName",
            dto.newUsername()
        );
    }
}
