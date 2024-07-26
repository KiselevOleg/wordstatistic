/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.service;

import com.example.wordstatistic.user.client.LocalStatisticService;
import com.example.wordstatistic.user.client.UsingHistoryService;
import com.example.wordstatistic.user.config.SecurityConfig;
import com.example.wordstatistic.user.dto.kafka.ChangeUsernameDTO;
import com.example.wordstatistic.user.dto.kafka.DeleteUserDTO;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.UserRepository;
import com.example.wordstatistic.user.util.RestApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Kiselev Oleg
 */
@Service
public class ChangeUserService {
    public static final RestApiException USER_NOT_FOUND_EXCEPTION =
        new RestApiException("user is not found", HttpStatus.NOT_FOUND);
    public static final RestApiException USER_NAME_FOUND_EXCEPTION =
        new RestApiException("a new name is already used", HttpStatus.CONFLICT);

    private static final String USER_ID_PARAMETER = "userId";
    private static final String OLD_NAME_USER_PARAMETER = "oldName";
    private static final String NEW_NAME_USER_PARAMETER = "newName";

    private final UserRepository userRepository;
    private final LocalStatisticService localStatisticService;
    private final UsingHistoryService usingHistory;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public ChangeUserService(
        final UserRepository userRepository,
        final LocalStatisticService localStatisticService,
        final UsingHistoryService usingHistory,
        final AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.localStatisticService = localStatisticService;
        this.usingHistory = usingHistory;
        this.authenticationManager = authenticationManager;
    }

    /**
     * change a user password.
     * @param userId a user's id
     * @param currentPassword a current password
     * @param newPassword a new password
     */
    public void changePassword(
        final UUID userId,
        final String currentPassword,
        final String newPassword
    ) throws RestApiException {
        final Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userId,
                currentPassword
            ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final User user = userRepository.findByUuid(userId).orElseThrow(() -> USER_NOT_FOUND_EXCEPTION);

        user.setPassword(SecurityConfig.passwordEncoder().encode(newPassword));
        userRepository.save(user);

        usingHistory.sendMessage(
            "changePassword",
            Map.of(
                USER_ID_PARAMETER, user.getId()
            ),
            Set.of(
                USER_ID_PARAMETER
            )
        );
    }

    /**
     * change a username.
     * @param userId a user's id
     * @param currentPassword a current password
     * @param newUsername a new username
     */
    public void changeUsername(
        final UUID userId,
        final String currentPassword,
        final String newUsername
    ) throws RestApiException {
        final Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userId,
                currentPassword
            ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (userRepository.existsByName(newUsername)) {
            throw USER_NAME_FOUND_EXCEPTION;
        }
        final User user = userRepository.findByUuid(userId).orElseThrow(() -> USER_NOT_FOUND_EXCEPTION);

        final String oldUsername = user.getName();
        user.setName(newUsername);
        userRepository.save(user);

        localStatisticService.send(new ChangeUsernameDTO(user.getUuid(), newUsername));

        usingHistory.sendMessage(
            "changeUsername",
            Map.of(
                USER_ID_PARAMETER, user.getId(),
                OLD_NAME_USER_PARAMETER, oldUsername,
                NEW_NAME_USER_PARAMETER, newUsername
            ),
            Set.of(
                USER_ID_PARAMETER
            )
        );
    }

    /**
     * delete a user.
     * @param userId a user's id
     * @param currentPassword a current password
     */
    public void deleteUser(
        final UUID userId,
        final String currentPassword
    ) throws RestApiException {
        final Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userId,
                currentPassword
            ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final User user = userRepository.findByUuid(userId).orElseThrow(() -> USER_NOT_FOUND_EXCEPTION);

        userRepository.delete(user);

        localStatisticService.send(new DeleteUserDTO(user.getUuid()));

        usingHistory.sendMessage(
            "deleteUser",
            Map.of(
                USER_ID_PARAMETER, user.getId()
            ),
            Set.of(
                USER_ID_PARAMETER
            )
        );
    }
}
