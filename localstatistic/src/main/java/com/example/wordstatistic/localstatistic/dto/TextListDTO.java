/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * a topic dto for getting a text list.
 * @param name a name of the text
 */
@Schema(description = "an text entity for a word list")
public record TextListDTO(
    @Schema(description = "a text name", example = "firstText")
    @JsonProperty("name") @NotBlank String name
) { }
