/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.repository.redis;

import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForUserCash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface GetMostPopularWordsListForUserCashRepository
    extends CrudRepository<GetMostPopularWordsListForUserCash, Long> {
    Optional<GetMostPopularWordsListForUserCash> findByUserId(UUID userId);
    //void deleteByUserId(UUID userId);
}
