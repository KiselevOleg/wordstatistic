/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.repository.redis;

import com.example.wordstatistic.globalstatistic.model.redis.GetPopularListResultCash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface GetPopularListResultCashRepository extends CrudRepository<GetPopularListResultCash, Long> {
    Optional<GetPopularListResultCash> findByLimit(Integer limit);
}
