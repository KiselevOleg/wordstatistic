/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * a word dto for local statistics.
 * @param name a word name
 * @param count a count of the word
 */
@Schema(description = "an word entity")
@Validated
public record WordDTO(
    @Schema(description = "word", example = "good")
    @Pattern(regexp = "^[a-z]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("name") @NotBlank String name,
    @Schema(description = "count", example = "143")
    @JsonProperty("count") @Min(0) Integer count
) implements Serializable { }
