package com.example.wordstatistic.localstatistic.controller;

import com.example.wordstatistic.localstatistic.dto.TextEntityDTO;
import com.example.wordstatistic.localstatistic.dto.TopicDTO;
import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
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
import java.util.UUID;

import static com.example.wordstatistic.localstatistic.service.LocalTextService.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TextControllerTest {
    @MockBean
    private LocalTextService localTextService;

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
    public void getAllTopicsForUserTest1() {
        final UUID user1 = UUID.randomUUID();
        when(localTextService.getAllTopicForUser(any())).thenReturn(
            List.of(
                new Topic(1, user1, "user1", "topic1"),
                new Topic(1, user1, "user1", "topic2"),
                new Topic(1, user1, "user1", "topic3"),
                new Topic(1, user1, "user1", "topic1")
            )
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getAllTopicsForUser?userId="+user1.toString(),
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
        final UUID user1 = UUID.randomUUID();
        when(localTextService.getAllTopicForUser(any())).thenReturn(
            List.of()
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getAllTopicsForUser?userId="+user1.toString(),
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
    public void getAllTextsForTopicTest1() throws RestApiException {
        UUID user1 = UUID.randomUUID();
        Topic topic = new Topic(1, user1, "user1", "topic1");
        when(localTextService.getAllTextsForSelectedTopic(any(),eq("topic1"))).thenReturn(
            List.of(
                new Text(1, topic, "text1", "a test text1"),
                new Text(3, topic, "text2", "a test text2"),
                new Text(4, topic, "text3", "a test text3")
            )
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getAllTextsForTopic?" +
                    "userId="+user1.toString()+"&topicName=topic1",
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
        UUID user1 = UUID.randomUUID();
        when(localTextService.getAllTextsForSelectedTopic(any(),eq("topic1"))).thenReturn(
            List.of()
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getAllTextsForTopic?userId="+user1.toString()+"&topicName=topic1",
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
        UUID user1 = UUID.randomUUID();
        when(localTextService.getAllTextsForSelectedTopic(any(),eq("topic1")))
            .thenThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR);

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getAllTextsForTopic?userId="+user1.toString()+"&topicName=topic1",
                String.class);
        assertEquals("incorrect result", HttpStatus.NOT_FOUND, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            "{\"message\":\"a topic is not found\"}",
            res
        );
    }

    @Test
    public void getTextContentTest1() throws RestApiException {
        UUID user1 = UUID.randomUUID();
        Topic topic = new Topic(1, user1, "user1", "topic1");
        when(localTextService.getTextForSelectedTextName(user1, "topic1", "text1")).thenReturn(
            Optional.of(new Text(1, topic, "text1", "a test text"))
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getTextContent?userId="+user1.toString()+"&topicName=topic1&textName=text1",
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
        UUID user1 = UUID.randomUUID();
        Topic topic = new Topic(1, user1, "user1", "topic1");
        when(localTextService.getTextForSelectedTextName(user1, "topic1", "text1")).thenReturn(
            Optional.empty()
        );

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getTextContent?userId="+user1.toString()+"&topicName=topic1&textName=text1",
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
        UUID user1 = UUID.randomUUID();
        when(localTextService.getTextForSelectedTextName(user1, "topic1", "text1"))
            .thenThrow(LocalTextService.TOPIC_OR_TEXT_NAME_NOT_FOUND_ERROR);

        ResponseEntity<String> response = testRestTemplate
            .getForEntity("http://localhost:"+this.port+"/topicsAndTexts/getTextContent?userId="+user1.toString()+"&topicName=topic1&textName=text1",
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
    public void addNewTopicTest1() throws RestApiException {
        UUID user1 = UUID.randomUUID();

        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> userNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .addTopic(userCap.capture(), userNameCap.capture(), topicNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewTopic?userId="+user1.toString()+"&username=user1",
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
            user1,
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
        UUID user1 = UUID.randomUUID();

        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> userNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_FOUND_ERROR).when(localTextService)
            .addTopic(userCap.capture(), userNameCap.capture(), topicNameCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TopicDTO requestObject = new TopicDTO("topic1");
        HttpEntity<TopicDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewTopic?userId="+user1.toString()+"&username=user1",
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
            user1,
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
    public void addNewTextTest1() throws RestApiException {
        UUID user1 = UUID.randomUUID();

        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(localTextService)
            .addText(userCap.capture(), topicNameCap.capture(), textNameCap.capture(), textCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText?userId="+user1.toString(),
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
            user1,
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
        UUID user1 = UUID.randomUUID();

        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TOPIC_NOT_FOUND_ERROR).when(localTextService)
            .addText(userCap.capture(), topicNameCap.capture(), textNameCap.capture(), textCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText?userId="+user1.toString(),
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
            user1,
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
        UUID user1 = UUID.randomUUID();

        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> topicNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> textCap = ArgumentCaptor.forClass(String.class);
        doThrow(LocalTextService.TEXT_EXISTS_ERROR).when(localTextService)
            .addText(userCap.capture(), topicNameCap.capture(), textNameCap.capture(), textCap.capture());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        TextEntityDTO requestObject = new TextEntityDTO("topic1", "text1", "a test text");
        HttpEntity<TextEntityDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .postForEntity("http://localhost:"+this.port+"/topicsAndTexts/addNewText?userId="+user1.toString(),
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
            user1,
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
}
