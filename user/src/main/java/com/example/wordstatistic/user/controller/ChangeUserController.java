/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.controller;

import com.example.wordstatistic.user.dto.ChangeUserPasswordDTO;
import com.example.wordstatistic.user.dto.ChangeUsernameDTO;
import com.example.wordstatistic.user.dto.DeleteUserDTO;
import com.example.wordstatistic.user.security.CustomUserDetailsService;
import com.example.wordstatistic.user.service.ChangeUserService;
import com.example.wordstatistic.user.util.RestApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Kiselev Oleg
 */
@RestController
@RequestMapping("/changeUser")
@Tag(name = "change user data controller", description = "a controller for changing user data")
@CrossOrigin(maxAge = 60L, origins = {"http://localhost", "http://localhost:80", "http://localhost:3000"})
@Validated
public class ChangeUserController {
    private final ChangeUserService changeUserService;

    @Autowired
    public ChangeUserController(
        final ChangeUserService changeUserService
    ) {
        this.changeUserService = changeUserService;
    }

    /**
     * if an input current user password is correct then replace a password with a new value.
     * @param dto user's password data
     * @return an error if it can not be executed
     */
    @Operation(
        summary = "change user password",
        description = "if an input current user password is correct then replace a password with a new value"
    )
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/changePassword", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(
        final @RequestBody @Parameter(description = "user's password data") @NotNull ChangeUserPasswordDTO dto
    ) {
        try {
            changeUserService.changePassword(
                CustomUserDetailsService.getUserId(),
                dto.currentPassword(),
                dto.newPassword()
            );
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RestApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * if an input current user password is correct then replace a username with a new value.
     * @param dto user's data
     * @return an error if it can not be executed
     */
    @Operation(
        summary = "change user password",
        description = "if an input current user password is correct then replace a username with a new value"
    )
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/changeUsername", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeUsername(
        final @RequestBody @Parameter(description = "user's data") @NotNull ChangeUsernameDTO dto
    ) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + dto);
        try {
            changeUserService.changeUsername(
                CustomUserDetailsService.getUserId(),
                dto.currentPassword(),
                dto.newUsername()
            );
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RestApiException e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * if an input current user password is correct then delete this user.
     * @param dto user's data
     * @return an error if it can not be executed
     */
    @Operation(
        summary = "change user password",
        description = "if an input current user password is correct then delete this user"
    )
    @SecurityRequirement(name = "JWT")
    @DeleteMapping(value = "/deleteUser", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUser(
        final @RequestBody @Parameter(description = "user's data") @NotNull DeleteUserDTO dto
    ) {
        try {
            changeUserService.deleteUser(
                CustomUserDetailsService.getUserId(),
                dto.currentPassword()
            );
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RestApiException e) {
            throw new RuntimeException(e);
        }
    }
}
