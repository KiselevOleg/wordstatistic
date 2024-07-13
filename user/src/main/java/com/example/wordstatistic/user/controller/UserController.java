/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.controller;

import com.example.wordstatistic.user.dto.TokenDTO;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.security.JwtTokenProvider;
import com.example.wordstatistic.user.service.UserService;
import com.example.wordstatistic.user.util.RestApiException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Kiselev Oleg
 */
@RestController
@RequestMapping("/registry")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(final UserService userService, final JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * sign up a new user by a name and a password.
     * @param userDto a userDto object (name, password)
     * @return exception if it can not be executed
     */
    @PostMapping(value = "/signUp", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signUp(final @RequestBody @NotNull UserDTO userDto) {
        try {
            userService.singUp(userDto);
        } catch (RestApiException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * sign in a user by a name and a password.
     * @param userDto a userDto object (name, password)
     * @return access and refresh tokens
     */
    @PostMapping(value = "/signIn", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenDTO> signIn(final @RequestBody @NotNull UserDTO userDto) {
        return new ResponseEntity<>(userService.singIn(userDto), HttpStatus.OK);
    }

    /**
     * refresh tokens.
     * @param tokenDTO a tokenDTO object (old access and refresh tokens)
     * @return access and refresh tokens
     */
    @PostMapping(value = "/refreshToken", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refreshToken(final @RequestBody @NotNull TokenDTO tokenDTO) {
        try {
            return new ResponseEntity<>(userService.refreshTokens(tokenDTO), HttpStatus.OK);
        } catch (RestApiException e) {
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
    }
}
