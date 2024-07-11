/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.repository;

import com.example.wordstatistic.user.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
}
