/**
 * Copyright 2024 Kiselev Oleg
 */
package com.example.wordstatistic.user.config;

import com.example.wordstatistic.user.security.JwtAuthenticationEntryPoint;
import com.example.wordstatistic.user.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Kiselev Oleg
 */
@Configuration
@EnableMethodSecurity
@SuppressWarnings("PMD.MultipleStringLiterals")
public class SecurityConfig {
    private static String passwordSalt = "qweiu_+h=ao237rv8bO&$^RBO&  Q#*vDFQA^M3t ergdogf67!4#^g&EDYHNfgb3";

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
    public static PasswordEncoder passwordEncoder() {
        return new CustomBCryptPasswordEncoder(12, passwordSalt);
    }
    public static void initSecurityContextHolder(
        final AuthenticationManager authenticationManager,
        final String userId,
        final String rawPassword
    ) {
        final Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userId,
                rawPassword + passwordSalt
            ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Bean
    SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((authorize) -> {
                authorize.requestMatchers(HttpMethod.POST, "/registry/signIn").permitAll();
                authorize.requestMatchers(HttpMethod.POST, "/registry/signUp").permitAll();
                authorize.requestMatchers(HttpMethod.POST, "/registry/refreshToken").permitAll();

                authorize.requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll();
                authorize.requestMatchers(HttpMethod.GET, "/v3/**").permitAll();

                authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                authorize.anyRequest().authenticated();
            });

        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint));
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private static class CustomBCryptPasswordEncoder extends BCryptPasswordEncoder {
        private final String salt;
        CustomBCryptPasswordEncoder(final Integer strenth, final String salt) {
            super(strenth);
            this.salt = salt;
        }

        /**
         * Encode the raw password. Generally, a good encoding algorithm applies a
         * SHA-1 or greater hash combined with an 8-byte or greater randomly generated salt.
         */
        public String encode(final CharSequence rawPawword) {
            return super.encode(rawPawword.toString() + salt);
        }
    }
}

/*/**
 * Copyright 2024 Kiselev Oleg
 *
package com.example.wordstatistic.user.config;

import com.example.wordstatistic.user.security.JwtAuthenticationEntryPoint;
import com.example.wordstatistic.user.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Kiselev Oleg
 *
@Configuration
@EnableMethodSecurity
public class SpringSecurityConfig {
    //private UserDetailsService userDetailsService;
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    public SpringSecurityConfig(
        final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        final JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.authenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.authenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests((authorize) -> {
//                    authorize.requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN");
//                    authorize.requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "USER");
//                    authorize.requestMatchers(HttpMethod.PATCH, "/api/**").hasAnyRole("ADMIN", "USER");
//                    authorize.requestMatchers(HttpMethod.GET, "/api/**").permitAll();
                authorize.requestMatchers("/api/auth/**").permitAll();
                authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                authorize.anyRequest().authenticated();
            }).httpBasic(Customizer.withDefaults());

        http.exceptionHandling( exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint));

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

//    @Bean
//    public UserDetailsService userDetailsService(){
//        final UserDetails haart = User.builder()
//                .username("haart")
//                .password(passwordEncoder().encode("password"))
//                .roles("ADMIN")
//                .build();
//        final UserDetails user = User.builder()
//                .username("user")
//                .password(passwordEncoder().encode("user"))
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(haart, user);
//    }
}*/

