/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.controller;

import com.example.wordstatistic.user.dto.TokenDTO;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.service.UserService;
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

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/signUp", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signUp(final @RequestParam String name, final @RequestParam String password) {
        final UserDTO userDto = new UserDTO(name, password);
        try {
            userService.singUp(userDto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/signIn", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenDTO> signIn(final @RequestParam String name, final @RequestParam String password) {
        final UserDTO userDto = new UserDTO(name, password);
        final String token = userService.singIn(userDto);

        final TokenDTO tokenDTO = new TokenDTO(token, "");

        return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
    }
}
