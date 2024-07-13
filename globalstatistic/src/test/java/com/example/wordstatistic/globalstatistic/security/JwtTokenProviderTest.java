package com.example.wordstatistic.globalstatistic.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.example.wordstatistic.globalstatistic.security.JwtTokenProvider.*;
import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtTokenProviderTest {
    private final JwtTokenProvider jwtTokenProvider;

    @LocalServerPort
    private Integer port;

    @Autowired
    public JwtTokenProviderTest(
        final JwtTokenProvider jwtTokenProvider
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private UUID userId;
    private String username;
    private Set<String> permissions;
    private String token;
    private String incorrectToken;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "user1";
        permissions = Set.of("per1", "per2");

        final Date currentDate = new Date();
        final Date expireDate = new Date(currentDate.getTime() + 10000L);

        token = Jwts.builder()
            .subject(userId.toString())
            .issuedAt(new Date())
            .expiration(expireDate)
            .claim(CLAIM_USERNAME, username)
            .claim(
                CLAIM_PERMISSIONS,
                permissions
            )
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET)))
            .compact();

        incorrectToken = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYj" +
            "IwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidX" +
            "Nlcm5hbWUiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX" +
            "0.M1f3knOHESqtrV1CPfQOyalq4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu";
    }
    @AfterEach
    void tearDown() { }

    @Test
    void getIdTest1() {
        assertEquals("incorrect result", userId, jwtTokenProvider.getId(token));
    }
    @Test
    void getIdTest2() {
        try {
            jwtTokenProvider.getId(incorrectToken);
        } catch (Exception e) {
            assertEquals("incorrect result", "JWT expired", e.getMessage().substring(0, 11));
            return;
        }

        fail("incorrect result");
    }

    @Test
    void getUsernameTest1() {
        assertEquals("incorrect result", username, jwtTokenProvider.getUsername(token));
    }
    @Test
    void getUsernameTest2() {
        try {
            jwtTokenProvider.getUsername(incorrectToken);
        } catch (Exception e) {
            assertEquals("incorrect result", "JWT expired", e.getMessage().substring(0, 11));
            return;
        }

        fail("incorrect result");
    }

    @Test
    void getPermissionsTest1() {
        assertEquals(
            "incorrect result",
            permissions,
            jwtTokenProvider.getPermissions(token)
        );
    }
    @Test
    void getPermissionsTest2() {
        try {
            jwtTokenProvider.getPermissions(incorrectToken);
        } catch (Exception e) {
            assertEquals("incorrect result", "JWT expired", e.getMessage().substring(0, 11));
            return;
        }

        fail("incorrect result");
    }

    @Test
    void validateTokenTest1() {
        assertEquals(
            "incorrect result",
            true,
            jwtTokenProvider.validateToken(token)
        );
    }
    @Test
    void validateTokenTest2() {
        try {
            jwtTokenProvider.validateToken(incorrectToken);
        } catch (Exception e) {
            assertEquals("incorrect result", "JWT expired", e.getMessage().substring(0, 11));
            return;
        }

        fail("incorrect result");
    }
}
