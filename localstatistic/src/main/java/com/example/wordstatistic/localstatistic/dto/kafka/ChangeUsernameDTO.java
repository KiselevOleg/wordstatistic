/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.dto.kafka;

import com.example.wordstatistic.localstatistic.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * a message to a localstatistic service about changing username.
 * @param userId a user's id
 * @param newUsername a username
 */
public record ChangeUsernameDTO(
    UUID userId,
    String newUsername
) {
    /**
     * transfer a message to a json.
     * @param dto a dto object
     * @return json
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    public static String toJSON(final ChangeUsernameDTO dto) throws KafkaDTOException, JsonProcessingException {
        if (dto == null) {
            throw new KafkaDTOException("UsingHistoryRecord instance is null");
        }
        if (dto.userId() == null) {
            throw new KafkaDTOException("userid is null");
        }
        if (dto.newUsername.isBlank()) {
            throw new KafkaDTOException("a new username is blank");
        }

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(dto);
        return json;
    }

    /**
     * get dto object from a json.
     * @param json the json
     * @return a dto object
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    public static ChangeUsernameDTO fromJSON(final String json) throws KafkaDTOException, JsonProcessingException {
        if (json == null) {
            throw new KafkaDTOException("a json string is null");
        }

        final ObjectMapper mapper = new ObjectMapper();
        final ChangeUsernameDTO dto = mapper.readValue(json, ChangeUsernameDTO.class);
        if (dto.newUsername.isBlank()) {
            throw new KafkaDTOException("a new username is blank");
        }
        return dto;
    }
}
