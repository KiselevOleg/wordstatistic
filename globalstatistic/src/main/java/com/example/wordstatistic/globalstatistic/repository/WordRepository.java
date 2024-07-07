/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.repository;

import com.example.wordstatistic.globalstatistic.model.Word;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Repository
@Validated
public interface WordRepository extends CrudRepository<Word, Integer> {
    Optional<Word> findByName(String name);
    Boolean existsByName(String name);
    @Query(value = "select * from word order by -count limit :limit", nativeQuery = true)
    @Transactional(readOnly = true)
    List<Word> getMostUsedWords(@Param("limit") @Min(1) @Max(1000) Integer limit);
}
