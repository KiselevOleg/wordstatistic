/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * a topic dto for getting a topic list.
 * @param name a name of the topic
 */
@Schema(description = "an topic entity")
@Validated
public record TopicDTO(
    @Schema(description = "a topic name", example = "ownTestTopic")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("name") @NotBlank String name
) { }
