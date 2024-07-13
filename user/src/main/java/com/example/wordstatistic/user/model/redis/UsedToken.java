/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.model.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

/**
 * @author Kiselev Oleg
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "Student", timeToLive = 4000L)
public class UsedToken implements Serializable {
    @Id
    @Indexed
    private Long id;
    @Indexed
    private String refreshToken;
    @TimeToLive
    private Long expirationInSeconds;
}
