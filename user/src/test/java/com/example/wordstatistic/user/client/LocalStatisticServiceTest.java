package com.example.wordstatistic.user.client;

import com.example.wordstatistic.user.dto.kafka.ChangeUsernameDTO;
import com.example.wordstatistic.user.dto.kafka.DeleteUserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocalStatisticServiceTest {
    private final LocalStatisticService localStatisticService;

    @MockBean
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public LocalStatisticServiceTest(
        final LocalStatisticService localStatisticService,
        final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.localStatisticService = localStatisticService;
        this.kafkaTemplate =kafkaTemplate;
    }

    @Test
    public void sendUpdateUserNameTest1() {
        ArgumentCaptor<String> messageNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageTextCap = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(messageNameCap.capture(),messageTextCap.capture())).thenReturn(null);

        final UUID userId = UUID.randomUUID();
        localStatisticService.send(new ChangeUsernameDTO(userId, "newName"));

        assertEquals("incorrect result", 1, messageNameCap.getAllValues().size());
        assertEquals("incorrect result", "changeUsername", messageNameCap.getValue());
        assertEquals(
            "incorrect result",
            "{\"userId\":\"" + userId + "\",\"newUsername\":\"newName\"}",
            messageTextCap.getValue()
        );
    }
    @Test
    public void sendDeleteUserNameTest1() {
        ArgumentCaptor<String> messageNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageTextCap = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(messageNameCap.capture(),messageTextCap.capture())).thenReturn(null);

        final UUID userId = UUID.randomUUID();
        localStatisticService.send(new DeleteUserDTO(userId));

        assertEquals("incorrect result", 1, messageNameCap.getAllValues().size());
        assertEquals("incorrect result", "deleteUser", messageNameCap.getValue());
        assertEquals(
            "incorrect result",
            "{\"userId\":\"" + userId + "\"}",
            messageTextCap.getValue()
        );
    }
}
