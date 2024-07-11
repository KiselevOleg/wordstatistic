/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.repository;

import com.example.wordstatistic.user.model.Permission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
