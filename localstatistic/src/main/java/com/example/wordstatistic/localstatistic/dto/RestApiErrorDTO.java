/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * a rest api error format.
 * @param message a description of the error
 */
public record RestApiErrorDTO(
    @JsonProperty("message") @NotBlank String message
) { }
