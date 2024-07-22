/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.util.remote.usingHistory;

/**
 * @author Kiselev Oleg
 */
public class UsingHistoryRecordIncorrectDataException extends Exception {
    public UsingHistoryRecordIncorrectDataException(final String message) {
        super("incorrect data " + message);
    }
}
