/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.repository;

import com.example.wordstatistic.localstatistic.model.Text;
import com.example.wordstatistic.localstatistic.model.Topic;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Repository
@Validated
public interface TextRepository extends CrudRepository<Text, Integer> {
    Optional<Text> findByTopicAndName(Topic topic, String name);
    List<Text> findAllByTopic(Topic topic);
}
