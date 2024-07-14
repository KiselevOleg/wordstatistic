/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * a word dto for local statistics.
 * @param name a word name
 * @param count a count of the word
 */
@Schema(description = "an word entity")
public record WordDTO(
    @Schema(description = "word", example = "good")
    @JsonProperty("name") @NotBlank String name,
    @Schema(description = "count", example = "143")
    @JsonProperty("count") @Min(0) Integer count
) { }
