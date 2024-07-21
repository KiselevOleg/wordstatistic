/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.usingHisotry.util;

/**
 * @author Kiselev Oleg
 */
public class UsingHistoryRecordIncorrectDataException extends Exception {
    public UsingHistoryRecordIncorrectDataException(final String message) {
        super("incorrect data " + message);
    }
}
