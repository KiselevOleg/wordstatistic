package com.example.wordstatistic.usingHisotry.service;

import com.example.wordstatistic.usingHisotry.model.UsingHistoryRecord;
import com.example.wordstatistic.usingHisotry.repository.UsingHistoryRepository;
import com.example.wordstatistic.usingHisotry.util.UsingHistoryRecordIncorrectDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsingHistoryServiceServiceTest {
    @MockBean
    private final UsingHistoryRepository usingHistoryRepository;
    private final UsingHistoryServiceService usingHistoryServiceService;

    @Autowired
    public UsingHistoryServiceServiceTest(
        final UsingHistoryRepository usingHistoryRepository,
        final UsingHistoryServiceService usingHistoryServiceService
    ) {
        this.usingHistoryRepository = usingHistoryRepository;
        this.usingHistoryServiceService = usingHistoryServiceService;
    }

    @Test
    public void addNewUsingHistoryRecordTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException, SQLException {
        ArgumentCaptor<UsingHistoryRecord> usingHistoryRecordCap
            = ArgumentCaptor.forClass(UsingHistoryRecord.class);
        doNothing().when(usingHistoryRepository).addRecord(usingHistoryRecordCap.capture());

        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord(
            "textService",
            "textOperation",
            Map.of(
                "data1", "value1",
                "data2", "value2"
            ),
            Set.of(
                "data1"
            )
        );
        final String serialized = UsingHistoryRecord.toJSON(usingHistoryRecord);

        usingHistoryServiceService.addNewUsingHistoryRecord(serialized);

        assertEquals("incorrect result", 1, usingHistoryRecordCap.getAllValues().size());
        assertEquals("incorrect result", usingHistoryRecord, usingHistoryRecordCap.getValue());
    }
    @Test
    public void addNewUsingHistoryRecordTest2() throws UsingHistoryRecordIncorrectDataException, SQLException {
        ArgumentCaptor<UsingHistoryRecord> usingHistoryRecordCap
            = ArgumentCaptor.forClass(UsingHistoryRecord.class);
        doNothing().when(usingHistoryRepository).addRecord(usingHistoryRecordCap.capture());

        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord(
            "usingHistory",
            "invalidMessage",
            Map.of(
                "exception_type", "",
                "error_message", "",
                "message", "y2ehwurijogkthlpmjo0tr9i48"
            ),
            Set.of(
                "exception_type"
            )
        );
        final String serialized = "y2ehwurijogkthlpmjo0tr9i48";

        usingHistoryServiceService.addNewUsingHistoryRecord(serialized);

        assertEquals("incorrect result", 1, usingHistoryRecordCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            usingHistoryRecord.getServiceName(),
            usingHistoryRecordCap.getValue().getServiceName()
        );
        assertEquals(
            "incorrect result",
            usingHistoryRecord.getHistoryTableName(),
            usingHistoryRecordCap.getValue().getHistoryTableName()
        );
        assertEquals(
            "incorrect result",
            true,
            Math.abs(
                usingHistoryRecordCap.getValue().getCreated().toInstant().getEpochSecond()
                - usingHistoryRecord.getCreated().toInstant().getEpochSecond()
            ) < 3L
        );

        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getShortData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getIntegerData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getLongData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getFloatData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getDoubleData().size()
        );
        assertEquals(
            "incorrect result",
            3,
            usingHistoryRecordCap.getValue().getStringData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getDateData().size()
        );

        assertEquals(
            "incorrect result",
            "y2ehwurijogkthlpmjo0tr9i48",
            usingHistoryRecordCap.getValue().getStringData().get("message")
        );
        assertEquals(
            "incorrect result",
            "Unrecognized token 'y2ehwurijogkthlpmjo0tr9i48': was expecting (JSON String, Number, " +
                "Array, Object or token 'null', 'true' or 'false')\n" +
                " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 27]",
            usingHistoryRecordCap.getValue().getStringData().get("error_message")
        );
        assertEquals(
            "incorrect result",
            "com.fasterxml.jackson.core.JsonParseException",
            usingHistoryRecordCap.getValue().getStringData().get("exception_type")
        );
    }
    @Test
    public void addNewUsingHistoryRecordTest3() throws
        UsingHistoryRecordIncorrectDataException,
        JsonProcessingException,
        NoSuchFieldException,
        IllegalAccessException, SQLException {
        ArgumentCaptor<UsingHistoryRecord> usingHistoryRecordCap
            = ArgumentCaptor.forClass(UsingHistoryRecord.class);
        doNothing().when(usingHistoryRepository).addRecord(usingHistoryRecordCap.capture());

        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord(
            "usingHistory",
            "invalidMessage",
            Map.of(
                "exception_type", "",
                "error_message", "",
                "message", ""
            ),
            Set.of(
                "exception_type"
            )
        );
        final UsingHistoryRecord usingHistoryRecordIncorrect = new UsingHistoryRecord(
            "usingHistory",
            "textOperation",
            Map.of(
                "data1", "value1",
                "data2", "value2"
            ),
            Set.of(
                "data1"
            )
        );
        final Field created = usingHistoryRecordIncorrect.getClass().getDeclaredField("created");
        created.setAccessible(true);
        created.set(usingHistoryRecordIncorrect, Date.from(Instant.ofEpochMilli(1721499644191L)));
        final String serialized = UsingHistoryRecord.toJSON(usingHistoryRecordIncorrect);

        usingHistoryServiceService.addNewUsingHistoryRecord(serialized);

        assertEquals("incorrect result", 1, usingHistoryRecordCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            usingHistoryRecord.getServiceName(),
            usingHistoryRecordCap.getValue().getServiceName()
        );
        assertEquals(
            "incorrect result",
            usingHistoryRecord.getHistoryTableName(),
            usingHistoryRecordCap.getValue().getHistoryTableName()
        );
        assertEquals(
            "incorrect result",
            true,
            Math.abs(
                usingHistoryRecordCap.getValue().getCreated().toInstant().getEpochSecond()
                    - usingHistoryRecord.getCreated().toInstant().getEpochSecond()
            ) < 3L
        );

        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getShortData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getIntegerData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getLongData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getFloatData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getDoubleData().size()
        );
        assertEquals(
            "incorrect result",
            3,
            usingHistoryRecordCap.getValue().getStringData().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecordCap.getValue().getDateData().size()
        );

        assertEquals(
            "incorrect result",
            "{\"serviceName\":\"usingHistory\",\"historyTableName\":\"textOperation\"," +
                "\"created\":1721499644191,\"shortData\":{},\"integerData\":{},\"longData\":{}," +
                "\"floatData\":{},\"doubleData\":{},\"stringData\":{\"data2\":\"value2\"," +
                "\"data1\":\"value1\"},\"dateData\":{},\"primaryKey\":[\"data1\"]}",
            usingHistoryRecordCap.getValue().getStringData().get("message")
        );
        assertEquals(
            "incorrect result",
            "incorrect data serviceName is usingHistory",
            usingHistoryRecordCap.getValue().getStringData().get("error_message")
        );
        assertEquals(
            "incorrect result",
            "com.example.wordstatistic.usingHisotry.util.UsingHistoryRecordIncorrectDataException",
            usingHistoryRecordCap.getValue().getStringData().get("exception_type")
        );
    }
}
