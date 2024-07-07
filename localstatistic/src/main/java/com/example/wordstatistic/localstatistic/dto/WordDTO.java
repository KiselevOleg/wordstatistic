/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * a word dto for local statistics.
 * @param name a word name
 * @param count a count of the word
 */
public record WordDTO(
    @JsonProperty("name") @NotBlank String name,
    @JsonProperty("count") @Min(0) Integer count
) { }
