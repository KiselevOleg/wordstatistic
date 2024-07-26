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
 * use dto object to delete a current user.
 * @param currentPassword a current active password
 */
@Schema(description = "use dto object to delete a current user")
@Validated
public record DeleteUserDTO(
    @Schema(description = "a current active password", example = "password123")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("currentPassword") String currentPassword
) { }
