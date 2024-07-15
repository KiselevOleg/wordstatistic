package com.example.wordstatistic.localstatistic.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.util.AssertionErrors.assertEquals;

class CustomerUserDetailsTest {
    private final UUID uuid = UUID.randomUUID();
    private final String name = "testName";
    private final String passwprd = "testPassword";
    private final Set<SimpleGrantedAuthority> authorities = Set.of(
        new SimpleGrantedAuthority("authority1"),
        new SimpleGrantedAuthority("authority2"),
        new SimpleGrantedAuthority("authority3")
    );

    @Test
    public void getAuthoritiesTest1() {
        final CustomerUserDetails user = new CustomerUserDetails(uuid, name, passwprd, authorities);
        assertEquals("incorrect result", authorities, user.getAuthorities());
    }
    @Test
    public void getAuthoritiesTest2() {
        final CustomerUserDetails user = new CustomerUserDetails(uuid, name, passwprd, Set.of());
        assertEquals("incorrect result", Set.of(), user.getAuthorities());
    }

    @Test
    public void getPasswordTest1() {
        final CustomerUserDetails user = new CustomerUserDetails(uuid, name, passwprd, authorities);
        assertEquals("incorrect result", passwprd, user.getPassword());
    }

    @Test
    public void getUsernameTest1() {
        final CustomerUserDetails user = new CustomerUserDetails(uuid, name, passwprd, authorities);
        assertEquals("incorrect result", name, user.getUsername());
    }
}
