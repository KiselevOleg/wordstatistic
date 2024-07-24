package com.example.wordstatistic.localstatistic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

/**
 * an entity text for deleting.
 * @param topic a topic name
 * @param name a text name
 */
@Schema(description = "an entity text for deleting")
@Validated
public record TextDeleteDTO(
    @Schema(description = "a topic name", example = "ownTestTopic")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("topic") String topic,
    @Schema(description = "a text name", example = "text1")
    @Length(min = 1, max = 50) @NotBlank
    @JsonProperty("name") String name
) { }
