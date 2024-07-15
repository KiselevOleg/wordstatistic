/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * a word dto.
 * @param name a word's record
 * @param count a word's count in a one of statistics
 */
@Schema(description = "an word entity")
@Validated
public record WordDTO(
    @Schema(description = "a word", example = "good")
    @Length(min = 1, max = 50)
    @JsonProperty("name") @NotBlank String name,
    @Schema(description = "count of the word", example = "143")
    @Min(0)
    @JsonProperty("count") @Min(0) Integer count
) { }
