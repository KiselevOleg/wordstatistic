package com.example.wordstatistic.globalstatistic.controller;

import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.globalstatistic.service.WordService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalStatisticTest {
    @MockBean
    private WordService wordService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

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
    public void getMostPopularWordsTest1() {
        when(wordService.getMostPopularWords(anyInt())).thenReturn(List.of(
            new Word(1, "w1",3),
            new Word(2, "w2",2),
            new Word(3, "w3",1)
        ));

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/globalStatistic/getMostPopularWords?limit=3",
                String.class);
        assertEquals("an incorrect http status", HttpStatus.OK, response.getStatusCode());

        final String body = response.getBody();
        assertNotEquals("incorrect data", null, body);
        assertEquals("incorrect data", "[{\"name\":\"w1\",\"count\":3},{\"name\":\"w2\",\"count\":2},{\"name\":\"w3\",\"count\":1}]", body);
    }
    @Test
    public void getMostPopularWordsTest2() {
        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:" + this.port + "/globalStatistic/getMostPopularWords?limit=0",
                String.class);
        assertEquals("an incorrect http status", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("an incorrect http status", "limit must be a positive integer", response.getBody());
    }

    @Test
    public void addTextTest1() {
        when(jwtTokenProvider.validateToken("adminToken")).thenReturn(true);
        when(jwtTokenProvider.getPermissions("adminToken")).thenReturn(Set.of("editText", "addTextToGlobal"));

        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/globalStatistic/addText?token=adminToken",
                "It's a test string for testing",
                String.class);
        assertEquals("an incorrect http status", HttpStatus.OK, response.getStatusCode());
    }
    @Test
    public void addTextTest2() {
        when(jwtTokenProvider.validateToken("userToken")).thenReturn(true);
        when(jwtTokenProvider.getPermissions("userToken")).thenReturn(Set.of("editText"));

        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/globalStatistic/addText?token=userToken",
                "It's a test string for testing",
                String.class);
        assertEquals("an incorrect http status", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("an incorrect http status", "invalid token", response.getBody());
    }
    @Test
    public void addTextTest3() {
        when(jwtTokenProvider.validateToken("userToken")).thenReturn(false);

        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/globalStatistic/addText?token=userToken",
                "It's a test string for testing",
                String.class);
        assertEquals("an incorrect http status", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("an incorrect http status", "invalid token", response.getBody());
    }
}
