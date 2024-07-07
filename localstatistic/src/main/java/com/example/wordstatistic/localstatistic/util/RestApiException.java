/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.util;

import com.example.wordstatistic.localstatistic.dto.RestApiErrorDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * @author Kiselev Oleg
 */
@Getter
@Setter
public class RestApiException extends Exception {
    private final HttpStatus status;

    public RestApiException(final String message, final HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }

    /**
     * transfer to a dto message.
     * @return a dto object
     */
    public RestApiErrorDTO toDTO() {
        return new RestApiErrorDTO(this.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status) + Objects.hashCode(this.getMessage());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RestApiException that = (RestApiException) o;
        return status == that.status && that.getMessage().equals(this.getMessage());
    }
}
