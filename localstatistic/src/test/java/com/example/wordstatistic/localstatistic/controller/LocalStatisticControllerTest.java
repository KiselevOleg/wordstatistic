package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.WordDTO;
import com.example.wordstatistic.localstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.localstatistic.service.LocalStatisticService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.wordstatistic.localstatistic.service.LocalStatisticService.TOPIC_NOT_FOUND_ERROR;
import static com.example.wordstatistic.localstatistic.service.LocalStatisticService.TEXT_NOT_FOUND_ERROR;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalStatisticControllerTest {
    @MockBean
    private LocalStatisticService localStatisticService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @LocalServerPort
    private Integer port;
    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private UUID userId;
    private String username;
    private Set<String> editTextPermissions;
    private Set<String> viewTextPermissions;
    private Set<String> nonePermissions;
    private String editTextToken;
    private String viewTextToken;
    private String noneToken;
    private String invalidToken;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "user1";
        editTextPermissions = Set.of("editText", "per2");
        viewTextPermissions = Set.of("per1", "viewText");
        nonePermissions = Set.of("per1", "per2", "per3");

        editTextToken = "editTextPermissions";
        when(jwtTokenProvider.validateToken(editTextToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(editTextToken)).thenReturn(editTextPermissions);
        when(jwtTokenProvider.getUsername(editTextToken)).thenReturn(username);
        when(jwtTokenProvider.getId(editTextToken)).thenReturn(userId);

        viewTextToken = "viewTextPermissions";
        when(jwtTokenProvider.validateToken(viewTextToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(viewTextToken)).thenReturn(viewTextPermissions);
        when(jwtTokenProvider.getUsername(viewTextToken)).thenReturn(username);
        when(jwtTokenProvider.getId(viewTextToken)).thenReturn(userId);

        noneToken = "noneToken";
        when(jwtTokenProvider.validateToken(noneToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(noneToken)).thenReturn(nonePermissions);
        when(jwtTokenProvider.getUsername(noneToken)).thenReturn(username);
        when(jwtTokenProvider.getId(noneToken)).thenReturn(userId);

        invalidToken = "incorrectToken";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        //when(jwtTokenProvider.getPermissions(incorrectToken)).thenThrow();
        //when(jwtTokenProvider.getUsername(incorrectToken)).thenThrow();
        //when(jwtTokenProvider.getId(incorrectToken)).thenThrow();
    }
    @AfterEach
    void tearDown() { }

    @Test
    public void getMostPopularWordsForUserTest1() {
        when(localStatisticService.getMostPopularWordsForUser(userId, 3)).thenReturn(
            List.of(
                new WordDTO("test", 15),
                new WordDTO("text", 12),
                new WordDTO("testing", 5)
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/localStatistic/getMostPopularWordsForUser?limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/localStatistic/getMostPopularWordsForUser?limit=0",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getMostPopularWordsForUserTest3() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.set("Authorization", "Bearer " + adminToken);
        headers.setBearerAuth(invalidToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/localStatistic/getMostPopularWordsForUser?limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getMostPopularWordsForUserTest4() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.set("Authorization", "Bearer " + adminToken);
        headers.setBearerAuth(noneToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/localStatistic/getMostPopularWordsForUser?limit=1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void getMostPopularWordsForTopicTest1() throws RestApiException {
        when(localStatisticService.getMostPopularWordsForTopic(userId, "topic1", 3)).thenReturn(
            List.of(
                new WordDTO("test", 15),
                new WordDTO("text", 12),
                new WordDTO("testing", 5)
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForTopic?topicName=topic1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
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
        when(localStatisticService.getMostPopularWordsForTopic(userId, "topic1", 3)).thenThrow(
            TOPIC_NOT_FOUND_ERROR
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForTopic?topicName=topic1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
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
    public void getMostPopularWordsForTopicTest3() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForTopic?topicName=topic1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getMostPopularWordsForTopicTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForTopic?topicName=topic1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void getMostPopularWordsForTextTest1() throws RestApiException {
        when(localStatisticService.getMostPopularWordsForText(userId, "topic1", "text1", 3)).thenReturn(
            List.of(
                new WordDTO("test", 15),
                new WordDTO("text", 12),
                new WordDTO("testing", 5)
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForText?topicName=topic1&textName=text1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
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
        when(localStatisticService.getMostPopularWordsForText(userId, "topic1", "text1", 3)).thenThrow(
            TEXT_NOT_FOUND_ERROR
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForText?topicName=topic1&textName=text1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "a text is not found",
            res
        );
    }
    @Test
    public void getMostPopularWordsForTextTest3() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForText?topicName=topic1&textName=text1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getMostPopularWordsForTextTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port
                    + "/localStatistic/getMostPopularWordsForText?topicName=topic1&textName=text1&limit=3",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
}
