/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.service;

import com.example.wordstatistic.globalstatistic.model.Word;
import com.example.wordstatistic.globalstatistic.repository.WordRepository;
import com.example.wordstatistic.globalstatistic.util.WordStatisticStringAnalysis;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Service
@Validated
public class WordService {
    private final WordRepository wordRepository;

    @Autowired
    WordService(final WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    /**
     * get a list of most used words in added texts.
     * @param limit size of the list
     * @return the list
     */
    public List<Word> getMostPopularWords(final @Min(1) Integer limit) {
        return wordRepository.getMostUsedWords(limit);
    }

    /**
     * add a new text.
     * @param text the text
     */
    @KafkaListener(topics = "text", groupId = "globalstatistic-group")
    public void addNewText(final @NotBlank String text) {
        WordStatisticStringAnalysis.getAllWords(text)
            .forEach(
                (w, n) -> wordRepository.findByName(w)
                    .or(() -> Optional.of(new Word(null, w, 0)))
                    .ifPresent(e -> {
                            e.setCount(e.getCount() + n);
                            wordRepository.save(e);
                        }
                    )
            );
    }
}
