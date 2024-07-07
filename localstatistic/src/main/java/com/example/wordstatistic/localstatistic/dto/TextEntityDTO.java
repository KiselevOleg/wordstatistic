/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * a topic dto for getting a text entity.
 * @param topic a owed text topic's name
 * @param name a name of the text
 * @param text a content of the text
 */
public record TextEntityDTO(
    @JsonProperty("topic") @NotBlank String topic,
    @JsonProperty("name") @NotBlank String name,
    @JsonProperty("text") @NotBlank String text
) { }
