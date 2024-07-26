/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * use dto object to change password.
 * @param currentPassword a current active password
 * @param newPassword a new password
 */
@Schema(description = "use dto object to change password")
@Validated
public record ChangeUserPasswordDTO(
    @Schema(description = "a current active password", example = "password123")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("currentPassword") String currentPassword,
    @Schema(description = "a new password", example = "termitYEt15_0")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("newPassword") String newPassword
) { }
