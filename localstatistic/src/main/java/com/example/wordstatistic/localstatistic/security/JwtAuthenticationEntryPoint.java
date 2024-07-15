/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Kiselev Oleg
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * AuthenticationEntryPoint is used by ExceptionTranslationFilter
     * to commence an authentication scheme. It is the entry point to
     * check if a user is authenticated and logs the person in or
     * throws an exception (unauthorized).
     * @param request a request
     * @param response a response
     * @param authException an exception
     * @throws IOException a possible exception
     * @throws ServletException a possible exception
     */
    @Override
    public void commence(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final AuthenticationException authException
    ) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}

