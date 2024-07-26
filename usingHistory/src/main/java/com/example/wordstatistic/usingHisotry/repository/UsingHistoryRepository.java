/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.usingHisotry.repository;

import com.clickhouse.jdbc.ClickHouseDataSource;
import com.example.wordstatistic.usingHisotry.model.UsingHistoryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author Kiselev Oleg
 */
@Repository
public class UsingHistoryRepository {
    private Connection conn;
    private Statement stmt;

    @Autowired
    public UsingHistoryRepository() {
        init();
    }

    public void addRecord(final UsingHistoryRecord usingHistoryRecord) throws SQLException {
        if (stmt == null) {
            init();
        }

        try {
            if (!existsTable(usingHistoryRecord)) {
                createTable(usingHistoryRecord);
            }

            addRecordToTable(usingHistoryRecord);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private String getTableName(final UsingHistoryRecord usingHistoryRecord) {
        return usingHistoryRecord.getServiceName() + "_"
            + usingHistoryRecord.getHistoryTableName();
    }

    private void addRecordToTable(final UsingHistoryRecord o) throws SQLException {
        final String query = addRecordToTableQuery(o);
        stmt.execute(query);
    }
    private String addRecordToTableQuery(final UsingHistoryRecord o) {
        final StringBuilder query = new StringBuilder();
        query.append("insert into ").append(getTableName(o)).append(" (");
        final List<Object> values = new ArrayList<>();
        addRecordToTableQueryAddParameters(query, o.getShortData(), values);
        addRecordToTableQueryAddParameters(query, o.getIntegerData(), values);
        addRecordToTableQueryAddParameters(query, o.getLongData(), values);
        addRecordToTableQueryAddParameters(query, o.getFloatData(), values);
        addRecordToTableQueryAddParameters(query, o.getDoubleData(), values);
        addRecordToTableQueryAddParameters(query, o.getStringData(), values);
        addRecordToTableQueryAddParameters(query, o.getDateData(), values);
        query.append("record_created");
        values.add(o.getCreated());
        query.append(") values (");
        for (Object v : values) {
            query.append(transferDataToStringFormat(v)).append(", ");
        }
        query.deleteCharAt(query.length() - 1);
        query.deleteCharAt(query.length() - 1);
        query.append(")").append("\n");

        return query.toString();
    }
    private String transferDataToStringFormat(Object v) {
        final String result;

        if (v.getClass().getName().equals("java.util.Date")) {
            result = transferDataToStringFormatForDate((Date) v);
        } else if (v.getClass().getName().equals("java.lang.String")) {
            final String string = (String) v;
            result = "'" + string.replace('\'', '\"') + "'";
        } else {
            result = v.toString();
        }

        return result;
    }
    private String transferDataToStringFormatForDate(final Date date) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        final StringBuilder res = new StringBuilder();
        res.append("'");

        final String year = String.valueOf(calendar.get(Calendar.YEAR));
        res.append(year);
        res.append("-");
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }
        res.append(month);
        res.append("-");
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day.length() == 1) {
            day = "0" + day;
        }
        res.append(day);

        res.append(" ");

        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        res.append(hour);
        res.append(":");
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        res.append(minute);
        res.append(":");
        String second = String.valueOf(calendar.get(Calendar.SECOND));
        if (second.length() == 1) {
            second = "0" + second;
        }
        res.append(second);

        res.append("'");
        return res.toString();
    }
    private void addRecordToTableQueryAddParameters(
        final StringBuilder query,
        final Map<String, ?> parameters,
        final List<Object> values
    ) {
        for (Map.Entry<String, ?> parameter : parameters.entrySet()) {
            query.append(parameter.getKey()).append(", ");
            values.add(parameter.getValue());
        }
    }

    private Boolean existsTable(final UsingHistoryRecord o) throws SQLException {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery("show tables");
            while (rs.next()) {
                if (getTableName(o).equals(rs.getString(1))) {
                    rs.close();
                    return true;
                }
            }
            rs.close();

            return false;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    private void createTable(final UsingHistoryRecord o) throws SQLException {
        final String query = createTableQuery(o);
        stmt.execute(query);
    }
    private String createTableQuery(final UsingHistoryRecord o) {
        final StringBuilder query = new StringBuilder();
        query.append("create table ").append(getTableName(o)).append("\n");
        query.append("(\n");
        createTableQueryAddColumns(query, "Int16", o.getShortData().keySet());
        createTableQueryAddColumns(query, "Int32", o.getIntegerData().keySet());
        createTableQueryAddColumns(query, "Int64", o.getLongData().keySet());
        createTableQueryAddColumns(query, "Float32", o.getFloatData().keySet());
        createTableQueryAddColumns(query, "Float64", o.getDoubleData().keySet());
        createTableQueryAddColumns(query, "String", o.getStringData().keySet());
        createTableQueryAddColumns(query, "DateTime('Europe/Moscow')", o.getDateData().keySet());
        query.append("record_created").append(" ").append("DateTime('Europe/Moscow')").append("\n");
        query.append(")\n");
        query.append("engine = MergeTree\n");
        query.append("primary key (");
        for (String key : o.getPrimaryKey()) {
            query.append(key + ", ");
        }
        query.deleteCharAt(query.length() - 1);
        query.deleteCharAt(query.length() - 1);
        query.append(")\n");

        return query.toString();
    }
    private void createTableQueryAddColumns(final StringBuilder query, final String type, final Set<String> names) {
        for (String name : names) {
            query.append(name).append(" ").append(type).append(",\n");
        }
    }

    private void init() {
        final String url = "jdbc:ch://usinghistory-clickhouse:8123/usingHistory"; //port 8123 by default

        final Properties properties = new Properties();

        try {
            final ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
            conn = dataSource.getConnection("haart", "test");
            stmt = conn.createStatement();
        } catch (SQLException e) {
            final Logger logger = LoggerFactory.getLogger(UsingHistoryRepository.class);
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!{}", e.getMessage());
        }
    }
}
