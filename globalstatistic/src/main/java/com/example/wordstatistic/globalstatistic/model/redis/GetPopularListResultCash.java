/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.model.redis;

import com.example.wordstatistic.globalstatistic.model.Word;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.List;

/**
 * @author Kiselev Oleg
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "GetPopularListResultCash", timeToLive = 60L)
public class GetPopularListResultCash implements Serializable {
    @Id
    @Indexed
    private Long id;
    @Indexed
    private Integer limit;
    private List<Word> result;
    @TimeToLive
    private Long expirationInSeconds;
}
