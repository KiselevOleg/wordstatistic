/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.model.redis;

import com.example.wordstatistic.localstatistic.dto.WordDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "GetMostPopularWordsListForTopicCash", timeToLive = 60L)
public class GetMostPopularWordsListForTopicCash implements Serializable {
    @Id
    @Indexed
    private Long id;
    @Indexed
    private Integer limit;
    @Indexed
    private UUID userId;
    @Indexed
    private Integer topicId;
    private List<WordDTO> result;
    @TimeToLive
    private Long expirationInSeconds;
}
