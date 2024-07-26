package com.example.wordstatistic.user.dto.kafka;

import com.example.wordstatistic.user.util.kafka.KafkaDTOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * a message to a localstatistic service about deleting a user.
 * @param userId a user's id
 */
@Validated
public record DeleteUserDTO(
    @NotNull UUID userId
) {
    /**
     * transfer a message to a json.
     * @param dto a dto object
     * @return json
     * @throws KafkaDTOException an exception if it can not be executed
     * @throws JsonProcessingException an exception if it can not be executed
     */
    public static String toJSON(final DeleteUserDTO dto) throws KafkaDTOException, JsonProcessingException {
        if (dto == null) {
            throw new KafkaDTOException("dto is null");
        }
        if (dto.userId == null) {
            throw new KafkaDTOException("userId is null");
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
    public static DeleteUserDTO fromJSON(final String json) throws KafkaDTOException, JsonProcessingException {
        if (json == null) {
            throw new KafkaDTOException("a json string is null");
        }

        final ObjectMapper mapper = new ObjectMapper();
        final DeleteUserDTO dto = mapper.readValue(json, DeleteUserDTO.class);
        if (dto.userId == null) {
            throw new KafkaDTOException("userId is null");
        }
        return dto;
    }
}
