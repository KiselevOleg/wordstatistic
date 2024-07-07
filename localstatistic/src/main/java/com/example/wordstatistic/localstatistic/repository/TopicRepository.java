/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.repository;

import com.example.wordstatistic.localstatistic.model.Topic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Repository
@Validated
public interface TopicRepository extends CrudRepository<Topic, Integer> {
    Optional<Topic> findByUserIdAndName(UUID userId, String name);
    List<Topic> findAllByUserId(UUID userId);
}
