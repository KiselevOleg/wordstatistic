package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * an entity text for updating.
 * @param topic a topic name
 * @param oldName a current text name
 * @param newName a new text name
 * @param text new text content (Optional.empty() if it is the same)
 */
@Schema(description = "an entity text for updating")
@Validated
public record TextUpdateDTO(
    @Schema(description = "a topic name", example = "ownTestTopic")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("topic") @NotBlank String topic,
    @Schema(description = "a current text name", example = "firstText")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("oldName") @NotBlank String oldName,
    @Schema(description = "a new text name", example = "text1")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "incorrect value")
    @Length(min = 1, max = 50)
    @JsonProperty("newName") @NotBlank String newName,
    @Schema(description = "new text content (null if it is the same)", example = "a test text")
    @JsonProperty("text") @NotNull Optional<String> text
) { }
