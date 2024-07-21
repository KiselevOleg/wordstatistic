package com.example.wordstatistic.usingHisotry.model;

import com.example.wordstatistic.usingHisotry.util.UsingHistoryRecordIncorrectDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

class UsingHistoryRecordTest {
    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private UsingHistoryRecord usingHistoryRecord;
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException, UsingHistoryRecordIncorrectDataException {
        usingHistoryRecord = new UsingHistoryRecord(
            "serviceName",
            "testOperation",
            Map.of(
                "data1", 1,
                "data2", Date.from(Instant.ofEpochMilli(1721381518481L))
            ),
            Set.of(
                "data1"
            )
        );

        final Date currentTime = Date.from(Instant.parse("2024-07-19T16:20:29Z"));
        final Field field = UsingHistoryRecord.class.getDeclaredField("created");
        field.setAccessible(true);
        field.set(usingHistoryRecord, currentTime);
    }
    @AfterEach
    void tearDown() { }

    @Test
    void toJSONStringTest1() throws UsingHistoryRecordIncorrectDataException, JsonProcessingException {
        final String serializedString = UsingHistoryRecord.toJSON(usingHistoryRecord);

        final String s = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":1},\"longData\":{}," +
            "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{\"data2\":1721381518481}," +
            "\"primaryKey\":[\"data1\"]}";
        assertEquals(
            "incorrect result",
            s,
            serializedString
        );
    }
    @Test
    void toJSONStringTest2() throws JsonProcessingException {
        try {
            UsingHistoryRecord.toJSON(null);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data UsingHistoryRecord instance is null",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void toJSONStringTest3() throws JsonProcessingException {
        try {
            UsingHistoryRecord.toJSON(new UsingHistoryRecord(
                "service",
                "table",
                Map.of(
                    "data1", 1,
                    "record_created", 2L
                ),
                Set.of(
                    "data1"
                )
            ));
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data parameter record_created is already used by default " +
                    "and can not be used in a parameter list",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }

    @Test
    void fromJSONStringTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        final String serializedString = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":1},\"longData\":{}," +
            "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{\"data2\":1721381518481}," +
            "\"primaryKey\":[\"data1\"]}";

        assertEquals(
            "incorrect result",
            usingHistoryRecord,
            UsingHistoryRecord.fromJSON(serializedString)
        );
    }
    @Test
    void fromJSONStringTest2() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        final String serializedString = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":1}," +
            "\"dateData\":{\"data2\":1721381518481},\"primaryKey\":[\"data1\"]}";

        assertEquals(
            "incorrect result",
            usingHistoryRecord,
            UsingHistoryRecord.fromJSON(serializedString)
        );
    }
    @Test
    void fromJSONStringTest3() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        final String serializedString = "{\"primaryKey\":[\"data1\"],\"historyTableName\":\"testOperation\"," +
            "\"serviceName\":\"serviceName\",\"dateData\":{\"data2\":1721381518481}," +
            "\"created\":1721406029000,\"integerData\":{\"data1\":1}}";

        assertEquals(
            "incorrect result",
            usingHistoryRecord,
            UsingHistoryRecord.fromJSON(serializedString)
        );
    }
    @Test
    void fromJSONStringTest4() throws UsingHistoryRecordIncorrectDataException {
        try {
            UsingHistoryRecord.fromJSON("19r8etguiojkfdsenrw45th");
        } catch (JsonProcessingException e) {
            assertEquals(
                "incorrect result",
                "Unexpected character ('r' (code 114)): Expected space separating root-level values\n" +
                    " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1, column: 3]",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest5() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON(null);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data a json string is null",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest6() throws UsingHistoryRecordIncorrectDataException {
        try {
            UsingHistoryRecord.fromJSON("{\"user\":\"test\",\"password\":\"test\"}");
        } catch (JsonProcessingException e) {
            assertEquals(
                "incorrect result",
                "Unrecognized field \"user\" (class com.example.wordstatistic.usingHisotry.model." +
                    "UsingHistoryRecord), not marked as ignorable (11 known properties: \"serviceName\", " +
                    "\"historyTableName\", \"shortData\", \"created\", \"doubleData\", \"primaryKey\", " +
                    "\"longData\", \"integerData\", \"stringData\", \"floatData\", \"dateData\"])\n" +
                    " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1," +
                    " column: 10] (through reference chain: com.example.wordstatistic.usingHisotry." +
                    "model.UsingHistoryRecord[\"user\"])",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest7() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\",\"created\":1721406029000," +
                "\"shortData\":{},\"integerData\":{\"data1\":1},\"longData\":{},\"floatData\":{}," +
                "\"doubleData\":{},\"stringData\":{},\"dateData\":{\"data2\":1721381518481}}");
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data historyTableName does not match [a-z][a-zA-Z0-9_]+",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest8() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\"," +
                "\"historyTableName\":\"testOperation\",\"created\":1721406029000,\"shortData\":{\"data1\":1}," +
                "\"integerData\":{\"data1\":1},\"dateData\":{\"data2\":1721381518481},\"primaryKey\":[\"data1\"]}");
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data there are several parameters with the same name",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest9() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\"," +
                "\"historyTableName\":\"testOperation\",\"created\":1721406029000,\"shortData\":{}," +
                "\"integerData\":{\"data1\":1,\"data1\":2},\"dateData\":{\"data2\":1721381518481}}");
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data primary key is empty",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest10() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\"," +
                "\"historyTableName\":\"testOperation\",\"created\":1721406029000,\"shortData\":{}," +
                "\"integerData\":{\"data1\":1,\"data1\":2},\"dateData\":{\"data2\":1721381518481}," +
                "\"primaryKey\":[\"data1\",\"data3\"]}");
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data primary key has a parameter name that does mot exist",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest11() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\"," +
                "\"historyTableName\":\"testOperation\",\"created\":1721406029000,\"shortData\":{}," +
                "\"integerData\":{\"data1\":1,\"data1\":2},\"dateData\":{\"data2\":1721381518481}," +
                "\"primaryKey\":[\"data1\",\"\"]}");
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data primary key has a parameter name that does mot exist",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest12() throws UsingHistoryRecordIncorrectDataException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\"," +
                "\"historyTableName\":\"testOperation\",\"created\":1721406029000,\"shortData\":{}," +
                "\"integerData\":{\"data1\":1,\"data1\":2},\"dateData\":{\"data2\":1721381518481}," +
                "\"primaryKey\":\"data3\"}");
        } catch (JsonProcessingException e) {
            assertEquals(
                "incorrect result",
                "Cannot construct instance of `java.util.HashSet` (although at least one Creator exists):" +
                    " no String-argument constructor/factory method to deserialize from String value ('data3')\n" +
                    " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 1," +
                    " column: 188] (through reference chain: com.example.wordstatistic.usingHisotry.model." +
                    "UsingHistoryRecord[\"primaryKey\"])",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    void fromJSONStringTest13() throws JsonProcessingException {
        try {
            UsingHistoryRecord.fromJSON("{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
                "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":1},\"longData\":{}," +
                "\"floatData\":{},\"doubleData\":{},\"stringData\":{\"record_created\":\"test\"}," +
                "\"dateData\":{\"data2\":1721381518481},\"primaryKey\":[\"data1\"]}");
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data parameter record_created is already used by default and " +
                    "can not be used in a parameter list",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }

    @Test
    void correctJSONSerializationTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        final String serializedString = UsingHistoryRecord.toJSON(usingHistoryRecord);

        assertEquals(
            "incorrect result",
            usingHistoryRecord,
            UsingHistoryRecord.fromJSON(serializedString)
        );
    }

    @Test
    public void createdDateTest1() {
        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord(
            "serviceName",
            "testOperation",
            Map.of(),
            Set.of()
        );
        final Date created = usingHistoryRecord.getCreated();

        assertEquals(
            "incorrectResult",
            true,
            Math.abs(
                created.toInstant().getEpochSecond()
                    - Instant.now().getEpochSecond()
            ) < 3L
        );
    }

    @Test
    public void setParameterTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        usingHistoryRecord.setParameter("data1", (Integer) null);

        final String s = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":null},\"longData\":{}," +
            "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{\"data2\":1721381518481}," +
            "\"primaryKey\":[\"data1\"]}";
        assertEquals(
            "incorrect result",
            s,
            UsingHistoryRecord.toJSON(usingHistoryRecord)
        );
    }
    @Test
    public void setParameterTest2() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        usingHistoryRecord.setParameter("dataShort", Short.valueOf("1"));
        usingHistoryRecord.setParameter("dataInteger", Integer.valueOf("0006"));
        usingHistoryRecord.setParameter("dataLong", Long.valueOf("-1"));
        usingHistoryRecord.setParameter("dataFloat", Float.valueOf("1.5"));
        usingHistoryRecord.setParameter("dataDouble", Double.valueOf("0.3333333333333333333333333333333333333"));
        usingHistoryRecord.setParameter("dataString", "test text string");
        usingHistoryRecord.setParameter("dataDate", Date.from(Instant.ofEpochSecond(1721382000L)));

        final String s = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{\"dataShort\":1},\"integerData\":{\"data1\":1," +
            "\"dataInteger\":6},\"longData\":{\"dataLong\":-1},\"floatData\":{\"dataFloat\":1.5}," +
            "\"doubleData\":{\"dataDouble\":0.3333333333333333},\"stringData\":{\"dataString\":\"test text string\"}," +
            "\"dateData\":{\"dataDate\":1721382000000,\"data2\":1721381518481},\"primaryKey\":[\"data1\"]}";
        assertEquals(
            "incorrect result",
            s,
            UsingHistoryRecord.toJSON(usingHistoryRecord)
        );
    }
    @Test
    public void setParameterTest3() throws JsonProcessingException {
        usingHistoryRecord.setParameter("dataShort", Short.valueOf("1"));
        usingHistoryRecord.setParameter("dataShort", Integer.valueOf("0006"));

        try {
            UsingHistoryRecord.toJSON(usingHistoryRecord);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data there are several parameters with the same name",
                e.getMessage()
            );

            return;
        }

        fail("incorrect result");
    }
    @Test
    public void setParameterTest4() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        usingHistoryRecord.setParameter("data1", Short.valueOf("1"));

        try {
            UsingHistoryRecord.toJSON(usingHistoryRecord);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data there are several parameters with the same name",
                e.getMessage()
            );

            return;
        }

        fail("incorrect result");
    }

    @Test
    public void addPrimaryKeyTest1() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        usingHistoryRecord.addPrimaryKey("data1");

        final String s = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":1},\"longData\":{}," +
            "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{\"data2\":1721381518481}," +
            "\"primaryKey\":[\"data1\"]}";
        assertEquals(
            "incorrect result",
            s,
            UsingHistoryRecord.toJSON(usingHistoryRecord)
        );
    }
    @Test
    public void addPrimaryKeyTest2() throws JsonProcessingException, UsingHistoryRecordIncorrectDataException {
        usingHistoryRecord.addPrimaryKey("data2");

        final String s = "{\"serviceName\":\"serviceName\",\"historyTableName\":\"testOperation\"," +
            "\"created\":1721406029000,\"shortData\":{},\"integerData\":{\"data1\":1},\"longData\":{}," +
            "\"floatData\":{},\"doubleData\":{},\"stringData\":{},\"dateData\":{\"data2\":1721381518481}," +
            "\"primaryKey\":[\"data2\",\"data1\"]}";
        assertEquals(
            "incorrect result",
            s,
            UsingHistoryRecord.toJSON(usingHistoryRecord)
        );
    }
    @Test
    public void addPrimaryKeyTest3() throws JsonProcessingException {
        usingHistoryRecord.addPrimaryKey("data1");
        usingHistoryRecord.addPrimaryKey("data3");

        try {
            UsingHistoryRecord.toJSON(usingHistoryRecord);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data primary key has a parameter name that does mot exist",
                e.getMessage()
            );

            return;
        }

        fail("incorrect result");
    }
    @Test
    public void addPrimaryKeyTest4() throws JsonProcessingException {
        usingHistoryRecord.addPrimaryKey("data3");

        try {
            UsingHistoryRecord.toJSON(usingHistoryRecord);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data primary key has a parameter name that does mot exist",
                e.getMessage()
            );

            return;
        }

        fail("incorrect result");
    }

    @Test
    public void emptyConstructorTest1() throws JsonProcessingException {
        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord();
        final UsingHistoryRecord usingHistoryRecordEmpty = new UsingHistoryRecord(
            "",
            "",
            Map.of(),
            Set.of()
        );

        assertEquals(
            "incorrect result",
            usingHistoryRecordEmpty,
            usingHistoryRecord
        );
    }
    @Test
    public void emptyConstructorTest2() throws JsonProcessingException {
        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord();

        try {
            UsingHistoryRecord.toJSON(usingHistoryRecord);
        } catch (UsingHistoryRecordIncorrectDataException e) {
            assertEquals(
                "incorrect result",
                "incorrect data serviceName does not match [a-z][a-zA-Z0-9_]+",
                e.getMessage()
            );
            return;
        }

        fail("incorrect result");
    }
    @Test
    public void mainConstructorTest1() throws JsonProcessingException {
        final UsingHistoryRecord usingHistoryRecord = new UsingHistoryRecord(
            "test",
            "table",
            Map.of(
                "data1", 1,
                "data2", 3L,
                "data3", "text1",
                "data4", "text2",
                "data5", "text1"
            ),
            Set.of()
        );

        assertEquals(
            "incorrect result",
            "test",
            usingHistoryRecord.getServiceName()
        );
        assertEquals(
            "incorrect result",
            "table",
            usingHistoryRecord.getHistoryTableName()
        );
        assertEquals(
            "incorrect result",
            true,
            Math.abs(
                usingHistoryRecord.getCreated().toInstant().getEpochSecond()
                    - Instant.now().getEpochSecond()
            ) < 3L
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecord.getShortData().keySet().size()
        );
        assertEquals(
            "incorrect result",
            1,
            usingHistoryRecord.getIntegerData().keySet().size()
        );
        assertEquals(
            "incorrect result",
            1,
            usingHistoryRecord.getIntegerData().get("data1")
        );
        assertEquals(
            "incorrect result",
            1,
            usingHistoryRecord.getLongData().keySet().size()
        );
        assertEquals(
            "incorrect result",
            3L,
            usingHistoryRecord.getLongData().get("data2")
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecord.getFloatData().keySet().size()
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecord.getDoubleData().keySet().size()
        );
        assertEquals(
            "incorrect result",
            3,
            usingHistoryRecord.getStringData().keySet().size()
        );
        assertEquals(
            "incorrect result",
            "text1",
            usingHistoryRecord.getStringData().get("data3")
        );
        assertEquals(
            "incorrect result",
            "text2",
            usingHistoryRecord.getStringData().get("data4")
        );
        assertEquals(
            "incorrect result",
            "text1",
            usingHistoryRecord.getStringData().get("data5")
        );
        assertEquals(
            "incorrect result",
            0,
            usingHistoryRecord.getDateData().keySet().size()
        );
    }
}
