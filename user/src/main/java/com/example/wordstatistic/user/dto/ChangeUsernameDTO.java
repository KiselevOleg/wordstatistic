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
 * use dto object to change username.
 * @param currentPassword a current active password
 * @param newUsername a new username
 */
@Schema(description = "use dto object to change username")
@Validated
public record ChangeUsernameDTO(
    @Schema(description = "a current active password", example = "password123")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("currentPassword") String currentPassword,
    @Schema(description = "a new username", example = "user123")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("newUsername") String newUsername
) { }
