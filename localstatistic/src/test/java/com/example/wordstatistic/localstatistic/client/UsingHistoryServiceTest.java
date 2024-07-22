package com.example.wordstatistic.localstatistic.client;

import com.example.wordstatistic.localstatistic.model.remote.usingHistory.UsingHistoryRecord;
import com.example.wordstatistic.localstatistic.util.remote.usingHistory.UsingHistoryRecordIncorrectDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsingHistoryServiceTest {
    @MockBean
    private final KafkaTemplate<String, String> kafkaTemplate;

    private UsingHistoryService usinhHistory;

    @Autowired
    public UsingHistoryServiceTest(
        final KafkaTemplate<String, String> kafkaTemplate,
        final UsingHistoryService usinhHistory
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.usinhHistory = usinhHistory;
    }

    @Test
    public void sendMessageTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        ArgumentCaptor<String> messageNameCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageTextCap = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(messageNameCap.capture(),messageTextCap.capture())).thenReturn(null);

        usinhHistory.sendMessage(
            "testOperation3",
            Map.of(
                "d1", 1,
                "d2", 1L,
                "r3", 2,
                "r4", 2,
                "t1", "test text",
                "t3", Date.from(Instant.ofEpochMilli(100000000L))
            ),
            Set.of(
                "d1", "r3", "t3"
            )
        );

        assertEquals(
            "incorrect result",
            1,
            messageNameCap.getAllValues().size()
        );
        assertEquals(
            "incorrect result",
            "usingHistory",
            messageNameCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "{\"serviceName\":\"localStatistic\",\"historyTableName\":\"testOperation3\"," +
                "\"created\":" +
                UsingHistoryRecord.fromJSON(messageTextCap.getValue()).getCreated().toInstant().toEpochMilli() +
                ",\"shortData\":{},\"integerData\":{\"r3\":2,\"r4\":2,\"d1\":1},\"longData\":{\"d2\":1}," +
                "\"floatData\":{},\"doubleData\":{},\"stringData\":{\"t1\":\"test text\"}," +
                "\"dateData\":{\"t3\":100000000},\"primaryKey\":[\"r3\",\"d1\",\"t3\"]}",
            messageTextCap.getValue()
        );
        assertEquals(
            "incorrect result",
            true,
            Math.abs(
                UsingHistoryRecord.fromJSON(messageTextCap.getValue()).getCreated().toInstant().getEpochSecond() -
                    Instant.now().getEpochSecond()
            ) < 3L
        );
    }
}
