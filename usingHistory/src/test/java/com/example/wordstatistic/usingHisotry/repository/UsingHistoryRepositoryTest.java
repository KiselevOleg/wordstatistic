package com.example.wordstatistic.usingHisotry.repository;

import com.example.wordstatistic.usingHisotry.model.UsingHistoryRecord;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class UsingHistoryRepositoryTest {
    private final UsingHistoryRepository usingHistoryRepository;

    public UsingHistoryRepositoryTest() {
        this.usingHistoryRepository = new UsingHistoryRepository();
    }

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private Method addRecordToTableQueryMethod;
    private Method createTableQueryMethod;
    private Method transferDataToStringFormatMethod;
    private UsingHistoryRecord usingHistoryRecord;
    @BeforeEach
    void setUp() throws NoSuchMethodException {
        createTableQueryMethod =
            usingHistoryRepository.getClass().getDeclaredMethod(
                "createTableQuery", UsingHistoryRecord.class
            );
        createTableQueryMethod.setAccessible(true);
        addRecordToTableQueryMethod =
            usingHistoryRepository.getClass().getDeclaredMethod(
                "addRecordToTableQuery", UsingHistoryRecord.class
            );
        addRecordToTableQueryMethod.setAccessible(true);
        transferDataToStringFormatMethod =
            usingHistoryRepository.getClass().getDeclaredMethod(
                "transferDataToStringFormat", Object.class
            );
        transferDataToStringFormatMethod.setAccessible(true);

        usingHistoryRecord = new UsingHistoryRecord(
            "testService",
            "operationTest1",
            Map.of(
                "data1", Short.valueOf("1"),
                "data2", Short.valueOf("1"),
                "idata1", 2,
                "data3", 5L,
                "nameTest", "testName",
                "data10", Date.from(Instant.ofEpochMilli(1721381518481L)),
                "f15", 1.5f,
                "d20", 2.0
            ),
            Set.of(
                "data1",
                "f15",
                "nameTest"
            )
        );
    }
    @AfterEach
    void tearDown() { }

    @Test
    public void createTableQueryTest1() throws InvocationTargetException, IllegalAccessException {
        final String query = (String) createTableQueryMethod.invoke(usingHistoryRepository, usingHistoryRecord);

        assertEquals(
            "incorrect result",
            "create table testService_operationTest1\n" +
                "(\n" +
                "data2 Int16,\n" +
                "data1 Int16,\n" +
                "idata1 Int32,\n" +
                "data3 Int64,\n" +
                "f15 Float32,\n" +
                "d20 Float64,\n" +
                "nameTest String,\n" +
                "data10 DateTime('Europe/Moscow'),\n" +
                "record_created DateTime('Europe/Moscow')\n" +
                ")\n" +
                "engine = MergeTree\n" +
                "primary key (data1, nameTest, f15)\n",
            query
        );
    }
    @Test
    public void addRecordToTableQueryTest1() throws InvocationTargetException, IllegalAccessException {
        final String query = (String) addRecordToTableQueryMethod.invoke(usingHistoryRepository, usingHistoryRecord);

        assertEquals(
            "incorrect result",
            true,
            query.startsWith("insert into testService_operationTest1 " +
                "(data2, data1, idata1, data3, f15, d20, nameTest, data10, record_created) " +
                "values (1, 1, 2, 5, 1.5, 2.0, 'testName', '2024-07-19 12:31:58', '")
        );
        assertEquals(
            "incorrect result",
            true,
            query.endsWith("')\n")
        );
        assertEquals(
            "incorrect result",
            ("insert into testService_operationTest1 " +
                "(data2, data1, idata1, data3, f15, d20, nameTest, data10, record_created) " +
                "values (1, 1, 2, 5, 1.5, 2.0, 'testName', '2024-07-19 12:31:58', " +
                    "'2024-07-21 15:58:57')\n").length(),
            query.length()
        );
        //assertEquals(
        //    "incorrect result",
        //    "insert into testService_operationTest1 " +
        //        "(data2, data1, idata1, data3, f15, d20, nameTest, data10, record_created) " +
        //        "values (1, 1, 2, 5, 1.5, 2.0, 'testName', '2024-07-19 12:31:58', '2024-07-21 15:58:57')\n",
        //    query
        //);
    }

    @Test
    public void transferDataToStringFormatTest1() throws InvocationTargetException, IllegalAccessException {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2024);
        calendar.set(Calendar.MONTH, 7 - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 19);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 31);
        calendar.set(Calendar.SECOND, 58);
        final Date date = calendar.getTime();
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, date);

        assertEquals("incorrect result", "'2024-07-19 12:31:58'", res);
    }
    @Test
    public void transferDataToStringFormatTest2() throws InvocationTargetException, IllegalAccessException {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 1 - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        final Date date = calendar.getTime();
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, date);

        assertEquals("incorrect result", "'1970-01-01 01:00:00'", res);
    }
    @Test
    public void transferDataToStringFormatTest3() throws InvocationTargetException, IllegalAccessException {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 1 - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        final Date date = calendar.getTime();
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, date);

        assertEquals("incorrect result", "'1970-01-01 00:00:00'", res);
    }
    @Test
    public void transferDataToStringFormatTest4() throws InvocationTargetException, IllegalAccessException {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 12 - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        final Date date = calendar.getTime();
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, date);

        assertEquals("incorrect result", "'2000-12-01 23:59:59'", res);
    }
    @Test
    public void transferDataToStringFormatTest5() throws InvocationTargetException, IllegalAccessException {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 12 - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        final Date date = calendar.getTime();
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, date);

        assertEquals("incorrect result", "'2000-12-31 00:00:00'", res);
    }
    @Test
    public void transferDataToStringFormatTest6() throws InvocationTargetException, IllegalAccessException {
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, "test text");

        assertEquals("incorrect result", "'test text'", res);
    }
    @Test
    public void transferDataToStringFormatTest7() throws InvocationTargetException, IllegalAccessException {
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, 1);

        assertEquals("incorrect result", "1", res);
    }
    @Test
    public void transferDataToStringFormatTest8() throws InvocationTargetException, IllegalAccessException {
        final String res = (String) transferDataToStringFormatMethod.invoke(usingHistoryRepository, 1.5);

        assertEquals("incorrect result", "1.5", res);
    }
}
