/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.repository.redis;

import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTopicCash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface GetMostPopularWordsListForTopicCashRepository
    extends CrudRepository<GetMostPopularWordsListForTopicCash, Long> {
    Optional<GetMostPopularWordsListForTopicCash> findByUserIdAndTopicId(UUID userId, Integer topicId);
    void deleteByUserIdAndTopicIdLimitLessThan(UUID userId, Integer topicId, Integer limit);
}
