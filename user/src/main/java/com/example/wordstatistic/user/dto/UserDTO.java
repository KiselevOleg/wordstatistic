/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * use dto object to sign in or sign up.
 * @param name a username
 * @param password a user's password
 */
@Schema(description = "an user entity")
@Validated
public record UserDTO(
    @Schema(description = "a username", example = "user123")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("name") @NotBlank String name,
    @Schema(description = "a user's password", example = "password123")
    @JsonProperty("password") @NotBlank String password
) { }
