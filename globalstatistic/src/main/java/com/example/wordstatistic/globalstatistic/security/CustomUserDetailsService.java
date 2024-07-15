/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * get username from a token.
     * @return a username
     */
    public static String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    /**
     * get user's id from a token.
     * @return an id
     */
    public static UUID getId() {
        return ((CustomerUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getId();
    }

    /**
     * get a userDetails by a token.
     * @param token the token.
     * @return userDetails about a user
     * @throws UsernameNotFoundException an exception when user is not found
     */
    @Override
    public CustomerUserDetails loadUserByUsername(final String token) throws UsernameNotFoundException {
        final Set<String> p = new HashSet<>(jwtTokenProvider.getPermissions(token));

        final Set<GrantedAuthority> authorities = p.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

        return new CustomerUserDetails(
            jwtTokenProvider.getId(token),
            jwtTokenProvider.getUsername(token),
            "password",
            authorities
        );
    }
}
