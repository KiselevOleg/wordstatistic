/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.globalstatistic.config;

import com.example.wordstatistic.globalstatistic.security.JwtAuthenticationEntryPoint;
import com.example.wordstatistic.globalstatistic.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Kiselev Oleg
 */
@Configuration
@EnableMethodSecurity
@SuppressWarnings("PMD.MultipleStringLiterals")
public class SecurityConfig {
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    public SecurityConfig(
        final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        final JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.authenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.authenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((authorize) -> {
                authorize.requestMatchers(HttpMethod.GET, "/globalStatistic/getMostPopularWords").permitAll();

                authorize.requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/v3/**").permitAll();

                authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                authorize.anyRequest().authenticated();
            }).httpBasic(Customizer.withDefaults());

        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint));
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
