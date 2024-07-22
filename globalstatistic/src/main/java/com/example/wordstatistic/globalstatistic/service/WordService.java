/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.service;

import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.model.redis.GetPopularListResultCash;
import com.example.wordstatistic.globalstatistic.model.remote.usingHistory.UsingHistoryRecord;
import com.example.wordstatistic.globalstatistic.repository.WordRepository;
import com.example.wordstatistic.globalstatistic.repository.redis.GetPopularListResultCashRepository;
import com.example.wordstatistic.globalstatistic.util.WordStatisticStringAnalysis;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class WordService {
    private static final String HISTORY_SERVICE_NAME = "usingHistory";
    private static final String THIS_SERVICE_NAME = "globalStatistic";

    private final WordRepository wordRepository;
    private final GetPopularListResultCashRepository getPopularListResultCashRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    WordService(
        final WordRepository wordRepository,
        final GetPopularListResultCashRepository getPopularListResultCashRepository,
        final KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.wordRepository = wordRepository;
        this.getPopularListResultCashRepository = getPopularListResultCashRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * get a list of most used words in added texts.
     * @param limit size of the list
     * @return the list
     */
    public List<Word> getMostPopularWords(final @Min(1) Integer limit) {
        return getPopularListResultCashRepository.findByLimit(limit)
            .map(GetPopularListResultCash::getResult)
            .or(() -> {
                final List<Word> res = wordRepository.getMostUsedWords(limit);
                getPopularListResultCashRepository.save(
                    new GetPopularListResultCash(null, limit, res, null)
                );
                return Optional.of(res);
            })
            .map((e) -> {
                kafkaTemplate.send(HISTORY_SERVICE_NAME, UsingHistoryRecord.withIngnoreExceptionsToJSON(
                    new UsingHistoryRecord(
                        THIS_SERVICE_NAME,
                        "getMostPopularWords",
                        Map.of(
                            "limit", limit,
                            "accepted", e.size()
                        ),
                        Set.of(
                            "limit"
                        )
                    )
                ));
                return e;
            }).orElseThrow();
    }

    /**
     * add a new text.
     * @param text the text
     */
    @KafkaListener(topics = "text", groupId = "globalstatistic-group", concurrency = "2")
    public void addNewText(final @NotBlank String text) {
        final AtomicReference<Long> wordCount = new AtomicReference<>(0L);
        WordStatisticStringAnalysis.getAllWords(text)
            .forEach(
                (w, n) -> wordRepository.findByName(w)
                    .or(() -> Optional.of(new Word(null, w, 0)))
                    .ifPresent(e -> {
                            wordCount.set(wordCount.get() + e.getCount());
                            e.setCount(e.getCount() + n);
                            wordRepository.save(e);
                        }
                    )
            );

        kafkaTemplate.send(HISTORY_SERVICE_NAME, UsingHistoryRecord.withIngnoreExceptionsToJSON(
                new UsingHistoryRecord(
                    THIS_SERVICE_NAME,
                    "addNewText",
                    Map.of(
                        "text_length", text.length(),
                        "word_count", wordCount.get()
                    ),
                    Set.of(
                        "text_length"
                    )
                )
            ));
    }
}
