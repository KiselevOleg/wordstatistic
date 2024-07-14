/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * a topic dto for getting a text entity.
 * @param topic a owed text topic's name
 * @param name a name of the text
 * @param text a content of the text
 */
@Schema(description = "an text entity")
public record TextEntityDTO(
    @Schema(description = "a topic name", example = "ownTestTopic")
    @JsonProperty("topic") @NotBlank String topic,
    @Schema(description = "a text name", example = "firstText")
    @JsonProperty("name") @NotBlank String name,
    @Schema(description = "text content", example = "a test text")
    @JsonProperty("text") @NotBlank String text
) { }
