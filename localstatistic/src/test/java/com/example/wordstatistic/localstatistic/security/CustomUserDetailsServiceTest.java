package com.example.wordstatistic.localstatistic.security;

import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class CustomUserDetailsServiceTest {
    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private final UUID uuid = UUID.randomUUID();
    private final String name = "testName";
    @BeforeEach
    void setUp() {
        final CustomerUserDetails userDetails = new CustomerUserDetails(
            uuid,
            name,
            "password",
            Set.of()
        );
        final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        authenticationToken.setDetails(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
    @AfterEach
    void tearDown() {}

    @Test
    public void getUsernameTest1() {
        assertEquals("incorrect result", name, CustomUserDetailsService.getUsername());
    }

    @Test
    public void getIdTest1() {
        assertEquals("incorrect result", uuid, CustomUserDetailsService.getId());
    }
}
