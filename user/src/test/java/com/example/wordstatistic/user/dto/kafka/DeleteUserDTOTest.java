package com.example.wordstatistic.user.dto.kafka;

import com.example.wordstatistic.user.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

class DeleteUserDTOTest {
    @Test
    public void toJSONTest1() throws KafkaDTOException, JsonProcessingException {
        final UUID user = UUID.randomUUID();
        final DeleteUserDTO dto = new DeleteUserDTO(user);

        assertEquals(
            "incorrect result",
            "{\"userId\":\"" + user + "\"}",
            DeleteUserDTO.toJSON(dto)
        );
    }
    @Test
    public void toJSONTest2() throws KafkaDTOException, JsonProcessingException {
        final UUID user = null;
        final DeleteUserDTO dto = new DeleteUserDTO(user);

        try {
            DeleteUserDTO.toJSON(dto);
        } catch(KafkaDTOException e) {
            assertEquals("incorrect result", "userId is null" ,e.getMessage());
            return;
        }

        fail("incorrect result");
    }
    @Test
    public void fromJSONTest1() throws KafkaDTOException, JsonProcessingException {
        final String json = "{\"userId\":\"1ec3454c-ecad-467f-9c59-7ad954c108f7\"}";
        final DeleteUserDTO dto = DeleteUserDTO.fromJSON(json);

        assertEquals(
            "incorrect result",
            "1ec3454c-ecad-467f-9c59-7ad954c108f7",
            dto.userId().toString()
        );
    }
}
