/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * a topic dto for getting a text list.
 * @param name a name of the text
 */
public record TextListDTO(
    @JsonProperty("name") @NotBlank String name
) { }
