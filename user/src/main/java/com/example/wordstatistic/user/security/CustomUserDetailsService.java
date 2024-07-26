/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.security;

import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *  class that implements the UserDetailsService interface
 *  ( Spring security in-build interface) and provides
 *  an implementation for the loadUserByUername() method.
 * @author Kiselev Oleg
 */
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    /**
     * get username from a token.
     * @return a username
     */
    public static UUID getUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * get a userDetails by a username.
     * @param username the username identifying the user whose data is required.
     * @return userDetails about a user
     * @throws UsernameNotFoundException an exception when user is not found
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUuid(UUID.fromString(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not exists by username"));

        final Set<GrantedAuthority> authorities = user.getRole().getPermissions().stream()
            .map((role) -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
            //username,
            user.getUuid().toString(),
            user.getPassword(),
            authorities
        );
    }
}
