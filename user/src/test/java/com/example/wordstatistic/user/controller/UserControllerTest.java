package com.example.wordstatistic.user.controller;

import com.example.wordstatistic.user.dto.TokenDTO;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.service.UserService;
import com.example.wordstatistic.user.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    @MockBean
    private UserService userService;

    @LocalServerPort
    private Integer port;
    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    @BeforeEach
    void setUp() { }
    @AfterEach
    void tearDown() { }

    @Test
    public void signUpTest1() throws RestApiException {
        ArgumentCaptor<UserDTO> userDTOCap = ArgumentCaptor.forClass(UserDTO.class);
        doNothing().when(userService).singUp(
            userDTOCap.capture()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDTO requestObject = new UserDTO("user1", "password1");
        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/registry/signUp",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        assertEquals("incorrect result", 1, userDTOCap.getAllValues().size());
        assertEquals("incorrect result", new UserDTO("user1", "password1"), userDTOCap.getValue());
    }
    @Test
    public void signUpTest2() throws RestApiException {
        ArgumentCaptor<UserDTO> userDTOCap = ArgumentCaptor.forClass(UserDTO.class);
        doThrow(UserService.USER_FOUND_EXCEPTION).when(userService).singUp(
            userDTOCap.capture()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDTO requestObject = new UserDTO("user1", "password1");
        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/registry/signUp",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("incorrect result", "user is found", response.getBody());

        assertEquals("incorrect result", 1, userDTOCap.getAllValues().size());
        assertEquals("incorrect result", new UserDTO("user1", "password1"), userDTOCap.getValue());
    }

    @Test
    public void signInTest1() throws RestApiException {
        ArgumentCaptor<UserDTO> userDTOCap = ArgumentCaptor.forClass(UserDTO.class);
        when(userService.singIn(
            userDTOCap.capture()
        )).thenReturn(new TokenDTO("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ" +
            "0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbW" +
            "UiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyal" +
            "q4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5" +
            "MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzU" +
            "zMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserDTO requestObject = new UserDTO("user1", "password1");
        HttpEntity<UserDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/registry/signIn",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());
        assertEquals(
            "incorrect result",
            "{\"accessToken\":\"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5Mm" +
                "I1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA" +
                "3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbWUiOiJoYWFydCIsInBlcm" +
                "1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOy" +
                "alq4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu\",\"refreshToken\":\"eyJh" +
                "bGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1O" +
                "DQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzUzMX0.lNc6TBVBmRxlzuSex" +
                "8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b\"}",
            response.getBody()
        );

        assertEquals("incorrect result", 1, userDTOCap.getAllValues().size());
        assertEquals("incorrect result", new UserDTO("user1", "password1"), userDTOCap.getValue());
    }

    @Test
    public void refreshTokenTest1() throws RestApiException {
        ArgumentCaptor<TokenDTO> tokenDTOCap = ArgumentCaptor.forClass(TokenDTO.class);
        when(userService.refreshTokens(
            tokenDTOCap.capture()
        )).thenReturn(new TokenDTO("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY" +
            "5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA4MDI5MTAsImV4cCI6MTcyMDgwMjk3MCwidXNl" +
            "cm5hbWUiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.ftxoAP3i1Jot3UEjT" +
            "ow_jcw_KVv1nSJks58KhspLQwoTYHJabOMLio_Jfj2tT9HV" , "eyJhbGciOiJIUzM4NCJ9.eyJ" +
            "zdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA4MDI5MTAsImV4cCI" +
            "6MTcyMDgwNjUxMH0.BHlBwCe2zknovWeZ0VQs_OlTK51VXxUYIlO5MyGtvY1RnPOWkmatgqv3tBhUy6AY"
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TokenDTO requestObject = new TokenDTO("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ" +
            "0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbW" +
            "UiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyal" +
            "q4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5" +
            "MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzU" +
            "zMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b");
        HttpEntity<TokenDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/registry/refreshToken",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        assertEquals("incorrect result", 1, tokenDTOCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            new TokenDTO("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ" +
            "0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbW" +
            "UiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyal" +
            "q4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5" +
            "MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzU" +
            "zMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b"),
            tokenDTOCap.getValue()
        );
    }
    @Test
    public void refreshTokenTest2() throws RestApiException {
        ArgumentCaptor<TokenDTO> tokenDTOCap = ArgumentCaptor.forClass(TokenDTO.class);
        when(userService.refreshTokens(
            tokenDTOCap.capture()
        )).thenThrow(UserService.INVALID_REFRESH_TOKEN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TokenDTO requestObject = new TokenDTO("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ" +
            "0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbW" +
            "UiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyal" +
            "q4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5" +
            "MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzU" +
            "zMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b");
        HttpEntity<TokenDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/registry/refreshToken",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("incorrect result", "invalid refresh token", response.getBody());

        assertEquals("incorrect result", 1, tokenDTOCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            new TokenDTO("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ" +
                "0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbW" +
                "UiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOHESqtrV1CPfQOyal" +
                "q4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu", "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5" +
                "MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzU" +
                "zMX0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b"),
            tokenDTOCap.getValue()
        );
    }
}
