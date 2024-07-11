/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.repository;

import com.example.wordstatistic.user.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
