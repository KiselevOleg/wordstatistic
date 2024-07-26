package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * an entity topic for updating.
 * @param oldName a current topic name
 * @param newName a new topic name
 */
@Schema(description = "an entity topic for updating")
@Validated
public record TopicUpdateDTO(
    @Schema(description = "a current topic name", example = "ownTextTopic")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("oldName") @NotBlank String oldName,
    @Schema(description = "a new topic name", example = "commonTexts")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("newName") @NotBlank String newName
) { }
