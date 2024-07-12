/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.repository;

import com.example.wordstatistic.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(@NotBlank String name);
    Optional<User> findByUuid(@NotNull UUID uuid);
    Boolean existsByName(@NotBlank String name);
}
