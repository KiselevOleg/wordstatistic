/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.usingHisotry.model;

import com.example.wordstatistic.usingHisotry.util.UsingHistoryRecordIncorrectDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.io.*;
import java.time.Instant;
import java.util.*;

/**
 * create a message for adding into usingHistory service database.<br>
 * serviceName - a name of a sending service.<br>
 * historyTableName - a name of a standard operation what is happening.<br>
 * created - a datatime of creating a message.<br>
 * data - parameters of a message (table columns).<br>
 * a map < String, Object > (a parameter name -> a parameter value).<br>
 * each instance of the standard operation must have the same parameters any time.<br>
 * supported parameter types.<br>
 * Short.<br>
 * Integer.<br>
 * Long.<br>
 * Float.<br>
 * Double.<br>
 * String.<br>
 * Date.<br>
 * @author Kiselev Oleg
 */
@Getter
@EqualsAndHashCode
@ToString
@Validated
public class UsingHistoryRecord {
    private static final String NAME_FORMAT = "[a-z][a-zA-Z0-9_]+";

    private final String serviceName;
    private final String historyTableName;
    private final Date created;
    private final Map<String, Short> shortData;
    private final Map<String, Integer> integerData;
    private final Map<String, Long> longData;
    private final Map<String, Float> floatData;
    private final Map<String, Double> doubleData;
    private final Map<String, String> stringData;
    private final Map<String, Date> dateData;
    private final Set<String> primaryKey;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Optional<String> errorData = Optional.empty();

    public UsingHistoryRecord() {
        this.serviceName = "";
        this.historyTableName = "";
        created = Date.from(Instant.now());
        shortData = new HashMap<>();
        integerData = new HashMap<>();
        longData = new HashMap<>();
        floatData = new HashMap<>();
        doubleData = new HashMap<>();
        stringData = new HashMap<>();
        dateData = new HashMap<>();
        primaryKey = new HashSet<>();
    }
    public UsingHistoryRecord(
        final String serviceName,
        final String historyTableName,
        final Map<String, Object> data,
        final Set<String> primaryKey
    ) {
        this.serviceName = serviceName;
        this.historyTableName = historyTableName;
        created = Date.from(Instant.now());
        shortData = new HashMap<>();
        integerData = new HashMap<>();
        longData = new HashMap<>();
        floatData = new HashMap<>();
        doubleData = new HashMap<>();
        stringData = new HashMap<>();
        dateData = new HashMap<>();
        this.primaryKey = new HashSet<>(primaryKey);

        try {
            for (Map.Entry<String, Object> c : data.entrySet()) {
                setParameter(c.getKey(), c.getValue());
            }
        } catch (UsingHistoryRecordIncorrectDataException e) {
            errorData = Optional.of(e.getMessage());
        }
    }

    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final Short value) {
        shortData.put(name, value);
        return this;
    }
    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final Integer value) {
        integerData.put(name, value);
        return this;
    }
    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final Long value) {
        longData.put(name, value);
        return this;
    }
    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final Float value) {
        floatData.put(name, value);
        return this;
    }
    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final Double value) {
        doubleData.put(name, value);
        return this;
    }
    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final String value) {
        stringData.put(name, value);
        return this;
    }
    /**
     * set a parameter.
     * @param name a parameter name
     * @param value a parameter value
     * @return itself
     */
    public UsingHistoryRecord setParameter(final String name, final Date value) {
        dateData.put(name, value);
        return this;
    }
    private void setParameter(final String name, final Object value)
        throws UsingHistoryRecordIncorrectDataException {
        switch (value.getClass().getName()) {
            case "java.lang.Short":
                setParameter(name, (Short) value);
                break;
            case "java.lang.Integer":
                setParameter(name, (Integer) value);
                break;
            case "java.lang.Long":
                setParameter(name, (Long) value);
                break;
            case "java.lang.Float":
                setParameter(name, (Float) value);
                break;
            case "java.lang.Double":
                setParameter(name, (Double) value);
                break;
            case "java.lang.String":
                setParameter(name, (String) value);
                break;
            case "java.util.Date":
                setParameter(name, (Date) value);
                break;
            default:
                throw new UsingHistoryRecordIncorrectDataException("an incorrect type of a parameter");
        }
    }
    /**
     * add a parameter to a primary key.
     * @param name a parameter name
     * @return itself
     */
    public UsingHistoryRecord addPrimaryKey(final String name) {
        primaryKey.add(name);
        return this;
    }

    /**
     * serializing from a string.
     * @param serializedString the serialized string
     * @return an original object
     * @throws IOException an exception when serializing can not be finished
     * @throws ClassNotFoundException an exception when serializing can not be finished
     * @throws UsingHistoryRecordIncorrectDataException an exception when serializing can not be finished
     */
    public static UsingHistoryRecord fromJSON(final String serializedString)
        throws UsingHistoryRecordIncorrectDataException, JsonProcessingException {
        if (serializedString == null) {
           throw new UsingHistoryRecordIncorrectDataException("a json string is null");
        }

        final ObjectMapper mapper = new ObjectMapper();
        final UsingHistoryRecord usingHistoryRecord = mapper.readValue(serializedString, UsingHistoryRecord.class);
        final Optional<String> error = usingHistoryRecord.correctData();
        if (error.isPresent()) {
            throw new UsingHistoryRecordIncorrectDataException(error.get());
        }
        return usingHistoryRecord;
    }

    /**
     * serializing to a string.
     * @param usingHistoryRecord an original object
     * @return a serialized string
     * @throws IOException an exception when serializing can not be finished
     * @throws UsingHistoryRecordIncorrectDataException an exception when serializing can not be finished
     */
    public static String toJSON(final UsingHistoryRecord usingHistoryRecord)
        throws UsingHistoryRecordIncorrectDataException, JsonProcessingException {
        if (usingHistoryRecord == null) {
            throw new UsingHistoryRecordIncorrectDataException("UsingHistoryRecord instance is null");
        }
        final Optional<String> error = usingHistoryRecord.correctData();
        if (error.isPresent()) {
            throw new UsingHistoryRecordIncorrectDataException(error.get());
        }

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(usingHistoryRecord);
        return json;
    }

    private Optional<String> correctData() {
        final Set<String> names = new HashSet<>();
        return errorData
            .or(() -> {
                if (!serviceName.matches(NAME_FORMAT)) {
                    return Optional.of("serviceName does not match " + NAME_FORMAT);
                }
                return Optional.empty();
            })
            .or(() -> {
                if (!historyTableName.matches(NAME_FORMAT)) {
                    return Optional.of("historyTableName does not match " + NAME_FORMAT);
                }
                return Optional.empty();
            })
            .or(() -> correctData(names, shortData))
            .or(() -> correctData(names, integerData))
            .or(() -> correctData(names, longData))
            .or(() -> correctData(names, floatData))
            .or(() -> correctData(names, doubleData))
            .or(() -> correctData(names, stringData))
            .or(() -> correctData(names, dateData))
            .or(() -> correctData(names, primaryKey))
            .or(() -> {
                if (names.contains("record_created")) {
                    return Optional.of("parameter record_created is already used by default "
                        + "and can not be used in a parameter list");
                }
                return Optional.empty();
            });
    }
    private Optional<String> correctData(final Set<String> names, final Map<String, ?> data) {
        Optional<String> error = Optional.empty();
        for (Map.Entry<String, ?> c : data.entrySet()) {
            if (c.getKey().isBlank()) {
                error = Optional.of("parameter name is blank");
                break;
            }
            if (!c.getKey().matches(NAME_FORMAT)) {
                error = Optional.of("parameter name does not match " + NAME_FORMAT);
                break;
            }
            if (names.contains(c.getKey())) {
                error = Optional.of("there are several parameters with the same name");
                break;
            }
            names.add(c.getKey());
        }
        return error;
    }
    private Optional<String> correctData(final Set<String> names, final Set<String> primaryKey) {
        Optional<String> error = Optional.empty();
        if (primaryKey.isEmpty()) {
            error = Optional.of("primary key is empty");
        }
        for (String c : primaryKey) {
            if (!names.contains(c)) {
                error = Optional.of("primary key has a parameter name that does mot exist");
                break;
            }
        }
        return error;
    }
}
