/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * use dto object to sign in or sign up.
 * @param name a username
 * @param password a user's password
 */
public record UserDTO(
    @JsonProperty("name") @NotBlank String name,
    @JsonProperty("password") @NotBlank String password
) { }
