/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.service;

import com.example.wordstatistic.user.config.SecurityConfig;
import com.example.wordstatistic.user.dto.TokenDTO;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.Role;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.PermissionRepository;
import com.example.wordstatistic.user.repository.RoleRepository;
import com.example.wordstatistic.user.repository.UserRepository;
import com.example.wordstatistic.user.security.JwtTokenProvider;
import com.example.wordstatistic.user.util.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Service
public class UserService {
    public static final RestApiException USER_FOUND_EXCEPTION =
        new RestApiException("user is found", HttpStatus.CONFLICT);
    public static final RestApiException INVALID_REFRESH_TOKEN =
        new RestApiException("invalid refresh token", HttpStatus.BAD_REQUEST);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public UserService(
        final AuthenticationManager authenticationManager,
        final JwtTokenProvider jwtTokenProvider,
        final UserRepository userRepository,
        final RoleRepository roleRepository,
        final PermissionRepository permissionRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * sign up a new user by a name and a password.
     * @param userDTO a user dto object
     * @throws Exception an exception if it can not be executed
     */
    public void singUp(final UserDTO userDTO) throws RestApiException {
        if (userRepository.existsByName(userDTO.name())) {
            throw USER_FOUND_EXCEPTION;
        }

        final String user = "user";
        final Role role = roleRepository.findByName(user).orElseGet(() -> {
            createDefaultRoles();
            return roleRepository.findByName(user).orElseThrow();
        });

        userRepository.save(
            new User(
                null,
                UUID.randomUUID(),
                userDTO.name(),
                SecurityConfig.passwordEncoder().encode(userDTO.password()),
                role
            )
        );
    }

    /**
     * sign in a user by a name and a password.
     * @param userDTO a user dto object
     * @throws Exception an exception if it can not be executed
     */
    public TokenDTO singIn(final UserDTO userDTO) {
        final Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            userDTO.name(),
            userDTO.password()
        ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String accessToken = jwtTokenProvider.generateAccessToken(userDTO.name());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(userDTO.name());

        return new TokenDTO(accessToken, refreshToken);
    }

    /**
     * refresh access and refresh tokens.
     * @param tokenDTO a tiken dto object (old access and refresh tokens)
     * @return a new access ans refresh tokens
     * @throws Exception an exception if it can not be executed
     */
    public TokenDTO refreshTokens(final TokenDTO tokenDTO) throws RestApiException {
        if (!jwtTokenProvider.validateRefreshToken(tokenDTO.refreshToken())) {
            throw INVALID_REFRESH_TOKEN;
        }

        final User user = userRepository.findByUuid(
            UUID.fromString(jwtTokenProvider.getRefreshUsername(tokenDTO.refreshToken()))
        ).orElseThrow();

        final String accessToken = jwtTokenProvider.generateAccessToken(user.getName());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(user.getName());

        return new TokenDTO(accessToken, refreshToken);
    }

    /**
     * create a default records in a database.
     */
    private void createDefaultRoles() {
        Permission viewText = new Permission(null, "viewText");
        permissionRepository.save(viewText);
        viewText = permissionRepository.findByName("viewText").orElseThrow();

        Permission editText = new Permission(null, "editText");
        permissionRepository.save(editText);
        editText = permissionRepository.findByName("editText").orElseThrow();

        Permission addTextToGlobal = new Permission(null, "addTextToGlobal");
        permissionRepository.save(addTextToGlobal);
        addTextToGlobal = permissionRepository.findByName("addTextToGlobal").orElseThrow();

        roleRepository.save(new Role(null, "user", Set.of(viewText, editText)));
        roleRepository.save(new Role(null, "admin", Set.of(viewText, editText, addTextToGlobal)));

        userRepository.save(new User(
            null,
            UUID.randomUUID(),
            "Haart",
            SecurityConfig.passwordEncoder().encode("test"),
            roleRepository.findByName("admin").orElseThrow()
        ));
    }
}
