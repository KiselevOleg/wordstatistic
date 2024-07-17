/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.repository.redis;

import com.example.wordstatistic.localstatistic.model.redis.GetMostPopularWordsListForTextCash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface GetMostPopularWordsListForTextCashRepository
    extends CrudRepository<GetMostPopularWordsListForTextCash, Long> {
    Optional<GetMostPopularWordsListForTextCash> findByUserIdAndTopicIdAndTextId(
        UUID userId, Integer topicId, Integer textId
    );
    void deleteByUserIdAndTopicIdAndTextIdAndLimitLessThan(
        UUID userId, Integer topicId, Integer textId, Integer limit
    );
}
