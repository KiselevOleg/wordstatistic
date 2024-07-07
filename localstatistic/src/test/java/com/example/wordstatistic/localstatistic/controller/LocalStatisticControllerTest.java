package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.WordDTO;
import com.example.wordstatistic.localstatistic.service.LocalStatisticService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
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
import java.util.UUID;

import static com.example.wordstatistic.localstatistic.service.LocalStatisticService.TOPIC_NOT_FOUND_ERROR;
import static com.example.wordstatistic.localstatistic.service.LocalStatisticService.TEXT_NOT_FOUND_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalStatisticControllerTest {
    @MockBean
    private LocalStatisticService localStatisticService;

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
    public void getMostPopularWordsForUserTest1() {
        final UUID user = UUID.randomUUID();
        when(localStatisticService.getMostPopularWordsForUser(user, 3)).thenReturn(
            List.of(
                new WordDTO("test", 15),
                new WordDTO("text", 12),
                new WordDTO("testing", 5)
            )
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+
                    "/localStatistic/getMostPopularWordsForUser?userId="+user.toString()+"&limit=3",
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[{\"name\":\"test\",\"count\":15},{\"name\":\"text\",\"count\":12},{\"name\":\"testing\",\"count\":5}]",
            res
        );
    }
    @Test
    public void getMostPopularWordsForUserTest2() {
        final UUID user = UUID.randomUUID();

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+
                    "/localStatistic/getMostPopularWordsForUser?userId="+user.toString()+"&limit=0",
                String.class);
        assertEquals("incorrect result", HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getMostPopularWordsForTopicTest1() throws RestApiException {
        final UUID user = UUID.randomUUID();
        when(localStatisticService.getMostPopularWordsForTopic(user, "topic1", 3)).thenReturn(
            List.of(
                new WordDTO("test", 15),
                new WordDTO("text", 12),
                new WordDTO("testing", 5)
            )
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+
                    "/localStatistic/getMostPopularWordsForTopic?userId="+user.toString()+"&topicName=topic1&limit=3",
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[{\"name\":\"test\",\"count\":15},{\"name\":\"text\",\"count\":12},{\"name\":\"testing\",\"count\":5}]",
            res
        );
    }
    @Test
    public void getMostPopularWordsForTopicTest2() throws RestApiException {
        final UUID user = UUID.randomUUID();
        when(localStatisticService.getMostPopularWordsForTopic(user, "topic1", 3)).thenThrow(
            TOPIC_NOT_FOUND_ERROR
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+
                    "/localStatistic/getMostPopularWordsForTopic?userId="+user.toString()+"&topicName=topic1&limit=3",
                String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "a topic is not found",
            res
        );
    }

    @Test
    public void getMostPopularWordsForTextTest1() throws RestApiException {
        final UUID user = UUID.randomUUID();
        when(localStatisticService.getMostPopularWordsForText(user, "topic1", "text1", 3)).thenReturn(
            List.of(
                new WordDTO("test", 15),
                new WordDTO("text", 12),
                new WordDTO("testing", 5)
            )
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+
                    "/localStatistic/getMostPopularWordsForText?userId="+user.toString()+"&topicName=topic1&textName=text1&limit=3",
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[{\"name\":\"test\",\"count\":15},{\"name\":\"text\",\"count\":12},{\"name\":\"testing\",\"count\":5}]",
            res
        );
    }
    @Test
    public void getMostPopularWordsForTextTest2() throws RestApiException {
        final UUID user = UUID.randomUUID();
        when(localStatisticService.getMostPopularWordsForTopic(user, "topic1", 3)).thenThrow(
            TEXT_NOT_FOUND_ERROR
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+
                    "/localStatistic/getMostPopularWordsForTopic?userId="+user.toString()+"&topicName=topic1&limit=3",
                String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "a text is not found",
            res
        );
    }
}
