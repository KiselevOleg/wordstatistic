/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * a topic dto for getting a topic list.
 * @param name a name of the topic
 */
@Schema(description = "an topic entity")
public record TopicDTO(
    @Schema(description = "a topic name", example = "ownTestTopic")
    @JsonProperty("name") @NotBlank String name
) { }
