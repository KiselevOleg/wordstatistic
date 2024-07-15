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
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    private UUID userId;
    private String username;
    private Set<String> adminPermissions;
    private Set<String> userPermissions;
    private String adminToken;
    private String userToken;
    private String incorrectToken;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "user1";
        adminPermissions = Set.of("addTextToGlobal", "per2");
        userPermissions = Set.of("per1", "per2");

        adminToken = "adminToken";

        when(jwtTokenProvider.validateToken(adminToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(adminToken)).thenReturn(adminPermissions);
        when(jwtTokenProvider.getUsername(adminToken)).thenReturn(username);
        when(jwtTokenProvider.getId(adminToken)).thenReturn(userId);

        userToken = "userToken";

        when(jwtTokenProvider.validateToken(userToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(userToken)).thenReturn(userPermissions);
        when(jwtTokenProvider.getUsername(userToken)).thenReturn(username);
        when(jwtTokenProvider.getId(userToken)).thenReturn(userId);

        incorrectToken = "incorrectToken";
        when(jwtTokenProvider.validateToken(incorrectToken)).thenReturn(false);
        //when(jwtTokenProvider.getPermissions(incorrectToken)).thenThrow();
        //when(jwtTokenProvider.getUsername(incorrectToken)).thenThrow();
        //when(jwtTokenProvider.getId(incorrectToken)).thenThrow();
    }
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
        assertEquals("an incorrect http status", HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void addTextTest1() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.set("Authorization", "Bearer " + adminToken);
        headers.setBearerAuth(adminToken);
        String requestObject = "It's a test string for testing";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/globalStatistic/addText",
                requestEntity,
                String.class);
        assertEquals("an incorrect http status", HttpStatus.OK, response.getStatusCode());
    }
    @Test
    public void addTextTest2() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userToken);
        String requestObject = "It's a test string for testing";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/globalStatistic/addText",
                requestEntity,
                String.class);
        assertEquals("an incorrect http status", HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @Test
    public void addTextTest3() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(incorrectToken);
        String requestObject = "It's a test string for testing";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/globalStatistic/addText",
                requestEntity,
                String.class);
        assertEquals("an incorrect http status", HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
