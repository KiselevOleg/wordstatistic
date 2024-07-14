/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.localstatistic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Kiselev Oleg
 */
@Configuration
@EnableMethodSecurity
@SuppressWarnings("PMD.MultipleStringLiterals")
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((authorize) -> {
                authorize.requestMatchers(
                    HttpMethod.GET, "/localStatistic/getMostPopularWordsForUser"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.GET, "/localStatistic/getMostPopularWordsForTopic"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.GET, "/localStatistic/getMostPopularWordsForText"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.GET, "/topicsAndTexts/getAllTopicsForUser"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.GET, "/topicsAndTexts/getAllTextsForTopic"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.GET, "/topicsAndTexts/getTextContent"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.POST, "/topicsAndTexts/addNewTopic"
                ).permitAll();
                authorize.requestMatchers(
                    HttpMethod.POST, "/topicsAndTexts/addNewText"
                ).permitAll();

                authorize.requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/v3/**").permitAll();

                authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                authorize.anyRequest().authenticated();
            });

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
