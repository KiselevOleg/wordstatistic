/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * a rest api error format.
 * @param message a description of the error
 */
@Schema(description = "an error entity")
public record RestApiErrorDTO(
    @Schema(description = "message of an error", example = "this username is found")
    @JsonProperty("message") @NotBlank String message
) { }
