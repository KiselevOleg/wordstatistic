/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * tokens for getting jwt access.
 * @param accessToken a token for getting access to an endpoint (can be used only ince)
 * @param refreshToken a token for getting a new tokens pair
 */
@Schema(description = "an token bunch entity")
public record TokenDTO(
    @Schema(
        description = "an access token for getting access for all resources (small live time)",
        example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00Nj"
            + "RmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidXNlcm5hbWU"
            + "iOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX0.M1f3knOH"
            + "ESqtrV1CPfQOyalq4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu"
    )
    @JsonProperty("accessToken") @NotBlank String accessToken,
    @Schema(
        description = "a refresh token for getting new tokens without user's data (can be used only once)",
        example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYjIwNy00NjRm"
            + "YTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzUzMX0.lNc6TBVBmRxlzuSe"
            + "x8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b"
    )
    @JsonProperty("refreshToken") @NotBlank String refreshToken
) { }
