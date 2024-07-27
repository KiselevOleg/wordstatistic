/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.service;

import com.example.wordstatistic.user.client.UsingHistoryService;
import com.example.wordstatistic.user.config.SecurityConfig;
import com.example.wordstatistic.user.dto.TokenDTO;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.Role;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.model.redis.UsedToken;
import com.example.wordstatistic.user.repository.PermissionRepository;
import com.example.wordstatistic.user.repository.RoleRepository;
import com.example.wordstatistic.user.repository.UserRepository;
import com.example.wordstatistic.user.repository.redis.UsedTokenRepository;
import com.example.wordstatistic.user.security.JwtTokenProvider;
import com.example.wordstatistic.user.util.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.Map;
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

    private static final String USER_NAME_PARAMETER = "userName";
    private static final String USER_ID_PARAMETER = "userId";
    private static final String USER_PASSWORD_LENGTH_PARAMETER = "userPasswordLength";
    private static final String SIGN_IN_STATUS_PARAMETER = "sinInStatus";
    private static final String REFRESH_TOKEN_STATUS_PARAMETER = "refreshTokenStatus";
    private final UsingHistoryService usingHistory;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private final UsedTokenRepository usedTokenRepository;

    @Autowired
    public UserService(
        final UsingHistoryService usingHistory,
        final AuthenticationManager authenticationManager,
        final JwtTokenProvider jwtTokenProvider,
        final UserRepository userRepository,
        final RoleRepository roleRepository,
        final PermissionRepository permissionRepository,
        final UsedTokenRepository usedTokenRepository
    ) {
        this.usingHistory = usingHistory;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.usedTokenRepository = usedTokenRepository;
    }

    /**
     * sign up a new user by a name and a password.
     * @param userDTO a user dto object
     * @throws Exception an exception if it can not be executed
     */
    public void singUp(final UserDTO userDTO) throws RestApiException {
        if (userRepository.existsByName(userDTO.name())) {
            usingHistory.sendMessage(
                "singUp_userFoundException",
                Map.of(
                    USER_NAME_PARAMETER, userDTO.name(),
                    USER_PASSWORD_LENGTH_PARAMETER, userDTO.password().length()
                ),
                Set.of(
                    USER_NAME_PARAMETER
                )
            );
            throw USER_FOUND_EXCEPTION;
        }

        final String user = "user";
        final Role role = roleRepository.findByName(user).orElseGet(() -> {
            createDefaultRoles();
            return roleRepository.findByName(user).orElseThrow();
        });

        final UUID userId = UUID.randomUUID();
        userRepository.save(
            new User(
                null,
                userId,
                userDTO.name(),
                SecurityConfig.passwordEncoder().encode(userDTO.password()),
                role
            )
        );
        usingHistory.sendMessage(
            "singUp",
            Map.of(
                USER_NAME_PARAMETER, userDTO.name(),
                USER_ID_PARAMETER, userId.toString()
            ),
            Set.of(
                USER_NAME_PARAMETER
            )
        );
    }

    /**
     * sign in a user by a name and a password.
     * @param userDTO a user dto object
     * @throws Exception an exception if it can not be executed
     */
    public TokenDTO singIn(final UserDTO userDTO) {
        usingHistory.sendMessage(
            "singIn",
            Map.of(
                USER_NAME_PARAMETER, userDTO.name(),
                SIGN_IN_STATUS_PARAMETER, "attempt"
            ),
            Set.of(
                USER_NAME_PARAMETER
            )
        );
        final String userId = userRepository.findByName(userDTO.name())
            .map(User::getUuid).map(UUID::toString).orElse("");
        SecurityConfig.initSecurityContextHolder(authenticationManager, userId, userDTO.password());

        final String accessToken = jwtTokenProvider.generateAccessToken(userDTO.name());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(userDTO.name());

        usingHistory.sendMessage(
            "singIn",
            Map.of(
                USER_NAME_PARAMETER, userDTO.name(),
                SIGN_IN_STATUS_PARAMETER, "success"
            ),
            Set.of(
                USER_NAME_PARAMETER
            )
        );
        return new TokenDTO(accessToken, refreshToken);
    }

    /**
     * refresh access and refresh tokens.
     * @param tokenDTO a tiken dto object (old access and refresh tokens)
     * @return a new access ans refresh tokens
     * @throws Exception an exception if it can not be executed
     */
    public TokenDTO refreshTokens(final TokenDTO tokenDTO) throws RestApiException {
        final String refreshTokenTableName = "refreshToken";
        usingHistory.sendMessage(
            refreshTokenTableName,
            Map.of(
                USER_NAME_PARAMETER, REFRESH_TOKEN_STATUS_PARAMETER,
                SIGN_IN_STATUS_PARAMETER, "attempt"
            ),
            Set.of(
                USER_NAME_PARAMETER
            )
        );
        if (!jwtTokenProvider.validateRefreshToken(tokenDTO.refreshToken())) {
            usingHistory.sendMessage(
                refreshTokenTableName,
                Map.of(
                    USER_NAME_PARAMETER, REFRESH_TOKEN_STATUS_PARAMETER,
                    SIGN_IN_STATUS_PARAMETER, "invalid_refresh_token"
                ),
                Set.of(
                    USER_NAME_PARAMETER
                )
            );
            throw INVALID_REFRESH_TOKEN;
        }
        if (usedTokenRepository.existsByRefreshToken(tokenDTO.refreshToken())) {
            usingHistory.sendMessage(
                refreshTokenTableName,
                Map.of(
                    USER_NAME_PARAMETER, REFRESH_TOKEN_STATUS_PARAMETER,
                    SIGN_IN_STATUS_PARAMETER, "refresh_token_already_used"
                ),
                Set.of(
                    USER_NAME_PARAMETER
                )
            );
            throw INVALID_REFRESH_TOKEN;
        }
        usedTokenRepository.save(new UsedToken(null, tokenDTO.refreshToken(), null));

        final User user = userRepository.findByUuid(
            UUID.fromString(jwtTokenProvider.getRefreshId(tokenDTO.refreshToken()))
        ).orElseThrow();

        final String accessToken = jwtTokenProvider.generateAccessToken(user.getName());
        final String refreshToken = jwtTokenProvider.generateRefreshToken(user.getName());

        usingHistory.sendMessage(
            refreshTokenTableName,
            Map.of(
                USER_NAME_PARAMETER, REFRESH_TOKEN_STATUS_PARAMETER,
                SIGN_IN_STATUS_PARAMETER, "success"
            ),
            Set.of(
                USER_NAME_PARAMETER
            )
        );

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
