package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.*;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import com.example.wordstatistic.localstatistic.security.JwtTokenProvider;
import com.example.wordstatistic.localstatistic.service.LocalTextService;
import com.example.wordstatistic.localstatistic.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TextControllerTest {
    @MockBean
    private LocalTextService localTextService;
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

        editTextToken = "editTextToken";
        when(jwtTokenProvider.validateToken(editTextToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(editTextToken)).thenReturn(editTextPermissions);
        when(jwtTokenProvider.getUsername(editTextToken)).thenReturn(username);
        when(jwtTokenProvider.getId(editTextToken)).thenReturn(userId);

        viewTextToken = "viewTextToken";
        when(jwtTokenProvider.validateToken(viewTextToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(viewTextToken)).thenReturn(viewTextPermissions);
        when(jwtTokenProvider.getUsername(viewTextToken)).thenReturn(username);
        when(jwtTokenProvider.getId(viewTextToken)).thenReturn(userId);

        noneToken = "noneToken";
        when(jwtTokenProvider.validateToken(noneToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(noneToken)).thenReturn(nonePermissions);
        when(jwtTokenProvider.getUsername(noneToken)).thenReturn(username);
        when(jwtTokenProvider.getId(noneToken)).thenReturn(userId);

        invalidToken = "invalidToken";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        //when(jwtTokenProvider.getPermissions(invalidToken)).thenThrow();
        //when(jwtTokenProvider.getUsername(invalidToken)).thenThrow();
        //when(jwtTokenProvider.getId(invalidToken)).thenThrow();
    }
    @AfterEach
    void tearDown() { }

    @Test
    public void getAllTopicsForUserTest1() {
        when(localTextService.getAllTopicForUser(any())).thenReturn(
            List.of(
                new Topic(1, userId, username, "topic1"),
                new Topic(1, userId, username, "topic2"),
                new Topic(1, userId, username, "topic3"),
                new Topic(1, userId, username, "topic1")
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTopicsForUser",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[{\"name\":\"topic1\"},{\"name\":\"topic2\"},{\"name\":\"topic3\"},{\"name\":\"topic1\"}]",
            res
        );
    }
    @Test
    public void getAllTopicsForUserTest2() {
        when(localTextService.getAllTopicForUser(any())).thenReturn(
            List.of()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTopicsForUser",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[]",
            res
        );
    }
    @Test
    public void getAllTopicsForUserTest3() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTopicsForUser",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getAllTopicsForUserTest4() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTopicsForUser",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void getAllTextsForTopicTest1() throws RestApiException {
        Topic topic = new Topic(1, userId, username, "topic1");
        when(localTextService.getAllTextsForSelectedTopic(any(),eq("topic1"))).thenReturn(
            List.of(
                new Text(1, topic, "text1", "a test text1"),
                new Text(3, topic, "text2", "a test text2"),
                new Text(4, topic, "text3", "a test text3")
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTextsForTopic?" +
                    "topicName=topic1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[{\"name\":\"text1\"},{\"name\":\"text2\"},{\"name\":\"text3\"}]",
            res
        );
    }
    @Test
    public void getAllTextsForTopicTest2() throws RestApiException {
        when(localTextService.getAllTextsForSelectedTopic(any(),eq("topic1"))).thenReturn(
            List.of()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTextsForTopic?" +
                    "topicName=topic1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "[]",
            res
        );
    }
    @Test
    public void getAllTextsForTopicTest3() throws RestApiException {
        when(localTextService.getAllTextsForSelectedTopic(any(),eq("topic1")))
            .thenThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTextsForTopic?" +
                    "topicName=topic1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("incorrect result", "{\"message\":\"a topic is not found\"}", response.getBody());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
    }
    @Test
    public void getAllTextsForTopicTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTextsForTopic?" +
                    "topicName=topic1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getAllTextsForTopicTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getAllTextsForTopic?" +
                    "topicName=topic1&token=viewTextToken",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void getTextContentTest1() throws RestApiException {
        Topic topic = new Topic(1, userId, username, "topic1");
        when(localTextService.getTextForSelectedTextName(userId, "topic1", "text1")).thenReturn(
            Optional.of(new Text(1, topic, "text1", "a test text"))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getTextContent?" +
                    "topicName=topic1&textName=text1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"topic\":\"topic1\",\"name\":\"text1\",\"text\":\"a test text\"}",
            res
        );
    }
    @Test
    public void getTextContentTest2() throws RestApiException {
        Topic topic = new Topic(1, userId, username, "topic1");
        when(localTextService.getTextForSelectedTextName(userId, "topic1", "text1")).thenReturn(
            Optional.empty()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getTextContent?" +
                    "topicName=topic1&textName=text1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "null",
            res
        );
    }
    @Test
    public void getTextContentTest3() throws RestApiException {
        when(localTextService.getTextForSelectedTextName(userId, "topic1", "text1"))
            .thenThrow(LocalTextService.TOPIC_OR_TEXT_NAME_NOT_FOUND_ERROR);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getTextContent?" +
                    "topicName=topic1&textName=text1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic or a text name is not found\"}",
            res
        );
    }
    @Test
    public void getTextContentTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getTextContent?" +
                    "topicName=topic1&textName=text1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void getTextContentTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        ResponseEntity<String> response = testRestTemplate
            .exchange(
                "http://localhost:" + this.port + "/topicsAndTexts/getTextContent?" +
                    "topicName=topic1&textName=text1",
                HttpMethod.GET,
                new HttpEntity<Object>(headers),
                String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void addNewTopicTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> userNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .addTopic(userCap.capture(), userNameCap.capture(), topicNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewTopic",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "user1",
            userNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
    }
    @Test
    public void addNewTopicTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> userNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_FOUND_ERROR).when(localTextService)
            .addTopic(userCap.capture(), userNameCap.capture(), topicNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewTopic",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.CONFLICT, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic already exists\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "user1",
            userNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
    }
    @Test
    public void addNewTopicTest3() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewTopic",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void addNewTopicTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewTopic",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void addNewTextTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .addText(userCap.capture(), topicNameCap.capture(), textNameCap.capture(), textCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "a test text",
            textCap.getValue()
        );
    }
    @Test
    public void addNewTextTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR).when(localTextService)
            .addText(userCap.capture(), topicNameCap.capture(), textNameCap.capture(), textCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "a test text",
            textCap.getValue()
        );
    }
    @Test
    public void addNewTextTest3() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TEXT_FOUND_ERROR).when(localTextService)
            .addText(userCap.capture(), topicNameCap.capture(), textNameCap.capture(), textCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.CONFLICT, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a text with this name already exists in the topic\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "a test text",
            textCap.getValue()
        );
    }
    @Test
    public void addNewTextTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void addNewTextTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText",
                requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void updateTopicTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicOldNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNewNameCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .updateTopic(userCap.capture(), topicOldNameCap.capture(), topicNewNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateTopic",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicOldNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "Topic1test",
            topicNewNameCap.getValue()
        );
    }
    @Test
    public void updateTopicTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicOldNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNewNameCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR).when(localTextService)
            .updateTopic(userCap.capture(), topicOldNameCap.capture(), topicNewNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateTopic",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicOldNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "Topic1test",
            topicNewNameCap.getValue()
        );
    }
    @Test
    public void updateTopicTest3() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateTopic",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void updateTopicTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateTopic",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void updateTopicTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateTopic",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void updateTextTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textOldNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNewNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Optional<String>> textContentCap = ArgumentCaptor.forClass(Optional.class);
        doNothing().when(localTextService)
            .updateText(
                userCap.capture(), topicNameCap.capture(),
                textOldNameCap.capture(), textNewNameCap.capture(),
                textContentCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextUpdateDTO requestObject = new TextUpdateDTO(
            "topic1", "text1", "text1new", Optional.of("text content")
        );
        HttpEntity<TextUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateText",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textOldNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1new",
            textNewNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            Optional.of("text content"),
            textContentCap.getValue()
        );
    }
    @Test
    public void updateTextTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textOldNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNewNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Optional<String>> textContentCap = ArgumentCaptor.forClass(Optional.class);
        doNothing().when(localTextService)
            .updateText(
                userCap.capture(), topicNameCap.capture(),
                textOldNameCap.capture(), textNewNameCap.capture(),
                textContentCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextUpdateDTO requestObject = new TextUpdateDTO(
            "topic1", "text1", "text1new", Optional.empty()
        );
        HttpEntity<TextUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateText",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textOldNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1new",
            textNewNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            Optional.empty(),
            textContentCap.getValue()
        );
    }
    @Test
    public void updateTextTest3() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textOldNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNewNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Optional<String>> textContentCap = ArgumentCaptor.forClass(Optional.class);
        doThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR).when(localTextService)
            .updateText(
                userCap.capture(), topicNameCap.capture(),
                textOldNameCap.capture(), textNewNameCap.capture(),
                textContentCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextUpdateDTO requestObject = new TextUpdateDTO(
            "topic1", "text1", "text1new", Optional.of("text content")
        );
        HttpEntity<TextUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateText",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textOldNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1new",
            textNewNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            Optional.of("text content"),
            textContentCap.getValue()
        );
    }
    @Test
    public void updateTextTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        TextUpdateDTO requestObject = new TextUpdateDTO(
            "topic1", "text1", "text1new", Optional.of("text content")
        );
        HttpEntity<TextUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateText",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void updateTextTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        TextUpdateDTO requestObject = new TextUpdateDTO(
            "topic1", "text1", "text1new", Optional.of("text content")
        );
        HttpEntity<TextUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateText",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void updateTextTest6() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        TextUpdateDTO requestObject = new TextUpdateDTO(
            "topic1", "text1", "text1new", Optional.of("text content")
        );
        HttpEntity<TextUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/updateText",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void deleteTopicTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .deleteTopic(userCap.capture(), topicNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteTopic",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
    }
    @Test
    public void deleteTopicTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR).when(localTextService)
            .deleteTopic(userCap.capture(), topicNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteTopic",
                HttpMethod.DELETE, requestEntity, String.class);

        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
    }
    @Test
    public void deleteTopicTest3() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteTopic",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void deleteTopicTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteTopic",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void deleteTopicTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        TopicUpdateDTO requestObject = new TopicUpdateDTO("topic1", "Topic1test");
        HttpEntity<TopicUpdateDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteTopic",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }

    @Test
    public void deleteTextTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .deleteText(userCap.capture(), topicNameCap.capture(), textNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextDeleteDTO requestObject = new TextDeleteDTO("topic1", "text1");
        HttpEntity<TextDeleteDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteText",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textNameCap.getValue()
        );
    }
    @Test
    public void deleteTextTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR).when(localTextService)
            .deleteText(userCap.capture(), topicNameCap.capture(), textNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(editTextToken);
        TextDeleteDTO requestObject = new TextDeleteDTO("topic1", "text1");
        HttpEntity<TextDeleteDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteText",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "topic1",
            topicNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "text1",
            textNameCap.getValue()
        );
    }
    @Test
    public void deleteTextTest3() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        TextDeleteDTO requestObject = new TextDeleteDTO("topic1", "text1");
        HttpEntity<TextDeleteDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteText",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void deleteTextTest4() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(viewTextToken);
        TextDeleteDTO requestObject = new TextDeleteDTO("topic1", "text1");
        HttpEntity<TextDeleteDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteText",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
    @Test
    public void deleteTextTest5() throws RestApiException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(noneToken);
        TextDeleteDTO requestObject = new TextDeleteDTO("topic1", "text1");
        HttpEntity<TextDeleteDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/topicsAndTexts/deleteText",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("incorrect result", true, response.getBody() == null);
    }
}
