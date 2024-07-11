/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * tokens for getting jwt access
 * @param accessToken a token for getting access to an endpoint (can be used only ince)
 * @param refreshToken a token for getting a new tokens pair
 */
public record TokenDTO(
    @JsonProperty("accessToken") @NotBlank String accessToken,
    @JsonProperty("refreshToken") @NotBlank String refreshToken

) { }
