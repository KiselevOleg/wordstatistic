/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * a word dto.
 * @param name a word's record
 * @param count a word's count in a one of statistics
 */
public record WordDTO(
    @JsonProperty("name") @NotBlank String name,
    @JsonProperty("count") @Min(0) Integer count
) { }
