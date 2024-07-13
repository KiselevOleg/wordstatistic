/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.repository.redis;

import com.example.wordstatistic.user.model.redis.UsedToken;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kiselev Oleg
 */
@Repository
public interface UsedTokenRepository extends CrudRepository<UsedToken, Long> {
    Boolean existsByRefreshToken(@NotBlank String refreshToken);
}
