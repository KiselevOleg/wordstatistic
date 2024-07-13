package com.example.wordstatistic.user.util;

import com.example.wordstatistic.user.dto.RestApiErrorDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class RestApiExceptionTest {
    @Test
    public void toDTOTest() {
        RestApiException restApiException = new RestApiException("data is not found", HttpStatus.NOT_FOUND);

        assertEquals("frasfter into a dto object is not correct",
            new RestApiErrorDTO("data is not found"),
            restApiException.toDTO()
        );
    }

    @Test
    public void equalsText1() {
        RestApiException o1 = new RestApiException("message", HttpStatus.BAD_GATEWAY);
        RestApiException o2 = new RestApiException("message", HttpStatus.BAD_GATEWAY);

        assertEquals("incorrect equals", true, o1.equals(o2));
    }
    @Test
    public void equalsText2() {
        RestApiException o1 = new RestApiException("message1", HttpStatus.BAD_GATEWAY);
        RestApiException o2 = new RestApiException("message2", HttpStatus.BAD_GATEWAY);

        assertEquals("incorrect equals", false, o1.equals(o2));
    }
    @Test
    public void equalsText3() {
        RestApiException o1 = new RestApiException("message", HttpStatus.BAD_GATEWAY);
        RestApiException o2 = new RestApiException("message", HttpStatus.NOT_FOUND);

        assertEquals("incorrect equals", false, o1.equals(o2));
    }
}
