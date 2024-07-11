/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.service;

import com.example.wordstatistic.user.config.SecurityConfig;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.Role;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.PermissionRepository;
import com.example.wordstatistic.user.repository.RoleRepository;
import com.example.wordstatistic.user.repository.UserRepository;
import com.example.wordstatistic.user.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author Kiselev Oleg
 */
@Service
public class UserService {
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

    public void singUp(final UserDTO userDTO) throws Exception {
        if (userRepository.existsByName(userDTO.name())) {
            throw new Exception("user is found");
        }

        final String user = "user";
        final Role role = roleRepository.findByName(user).orElseGet(() -> {
            createDefaultRoles();
            return roleRepository.findByName(user).orElseThrow();
        });

        userRepository.save(
            new User(null, userDTO.name(), SecurityConfig.passwordEncoder().encode(userDTO.password()), role)
        );
    }

    public String singIn(final UserDTO userDTO) {
        final Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            userDTO.name(),
            userDTO.password()
        ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

    private void createDefaultRoles() {
        Permission viewText = new Permission(null, "viewText");
        permissionRepository.save(viewText);
        viewText = permissionRepository.findByName("viewText").orElseThrow();
        Permission editText = new Permission(null, "editText");
        permissionRepository.save(editText);
        editText = permissionRepository.findByName("editText").orElseThrow();
        roleRepository.save(new Role(null, "user", Set.of(viewText, editText)));
        roleRepository.save(new Role(null, "admin", Set.of(viewText, editText)));
    }
}
