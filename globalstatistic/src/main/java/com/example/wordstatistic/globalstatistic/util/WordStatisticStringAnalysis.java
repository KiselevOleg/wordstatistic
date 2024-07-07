/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kiselev Oleg
 */
public final class WordStatisticStringAnalysis {
    private WordStatisticStringAnalysis() { }

    /**
     * get all words in a given text string.
     * @param text the text
     * @return a map with all words with fit counts
     */
    public static Map<String, Integer> getAllWords(final String text) {
        final Map<String, Integer> res = new HashMap<>();

        if (text == null) {
            return res;
        }

        Arrays.stream(text
                .toLowerCase()
                .replaceAll("[^[a-z ]]", " ")
                .trim()
                .replaceAll(" +", " ")
                .split(" "))
                .filter(e -> !e.isBlank())
                .forEach(e -> res.put(e, res.getOrDefault(e, 0) + 1));
        return res;
    }
}
