package com.example.wordstatistic.user.security;

import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.Role;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtTokenProviderTest {
    @MockBean
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @LocalServerPort
    private Integer port;

    @Autowired
    public JwtTokenProviderTest(
        final UserRepository userRepository,
        final JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private Permission permission1, permission2;
    private Role role;
    private User user;
    private String token, refreshToken;
    private String incorrectToken, incorrectRefreshToken;
    @BeforeEach
    void setUp() {
        permission1 = new Permission(1, "per1");
        permission2 = new Permission(2, "per2");
        role = new Role(
            1,
            "role1",
            Set.of(permission1, permission2)
        );
        user = new User(
            1,
            UUID.randomUUID(),
            "user1",
            "password1",
            role
        );

        when(userRepository.findByName("user1")).thenReturn(Optional.of(user));
        token = jwtTokenProvider.generateAccessToken("user1");
        refreshToken = jwtTokenProvider.generateRefreshToken("user1");
        when(userRepository.findByName("user1")).thenReturn(null);

        incorrectToken = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ0OWMtYj" +
            "IwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDc5OTk5MSwidX" +
            "Nlcm5hbWUiOiJoYWFydCIsInBlcm1pc3Npb25zIjpbInZpZXdUZXh0IiwiZWRpdFRleHQiXX" +
            "0.M1f3knOHESqtrV1CPfQOyalq4DLWaqrqEjmO0jffNuQ0FZYqCBqmHy-ieT6g68vu";
        incorrectRefreshToken = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5MmI1NWNiOS1hYjY5LTQ" +
            "0OWMtYjIwNy00NjRmYTk1ODQxNTUiLCJpYXQiOjE3MjA3OTk5MzEsImV4cCI6MTcyMDgwMzUzM" +
            "X0.lNc6TBVBmRxlzuSex8xoqeSppps0LVGPwwtIBTPS4za5s22_CLuFL7fZPlnyES2b";
    }
    @AfterEach
    void tearDown() { }

    @Test
    void generateAccessTokenTest1() {
        when(userRepository.findByName("user1")).thenReturn(Optional.of(user));

        final String token = jwtTokenProvider.generateAccessToken("user1");
        assertEquals(
            "incorrect result",
            true,
            jwtTokenProvider.validateToken(token)
        );
        assertEquals(
            "incorrect result",
            user.getUuid(),
            jwtTokenProvider.getId(token)
        );
        assertEquals(
            "incorrect result",
            user.getName(),
            jwtTokenProvider.getUsername(token)
        );
        assertEquals(
            "incorrect result",
            Set.of(permission1.getName(), permission2.getName()),
            jwtTokenProvider.getPermissions(token)
        );
    }
    @Test
    void generateAccessTokenTest2() {
        when(userRepository.findByName("user1")).thenReturn(Optional.empty());

        try {
            final String token = jwtTokenProvider.generateAccessToken("user1");
        } catch (Exception e) {
            assertEquals("incorrect result", "No value present", e.getMessage());
            return;
        }

        fail("incorrect result");
    }

    @Test
    void generateRefreshTokenTest1() {
        when(userRepository.findByName("user1")).thenReturn(Optional.of(user));

        final String token = jwtTokenProvider.generateRefreshToken("user1");
        assertEquals(
            "incorrect result",
            true,
            jwtTokenProvider.validateRefreshToken(token)
        );
        assertEquals(
            "incorrect result",
            user.getUuid().toString(),
            jwtTokenProvider.getRefreshId(token)
        );
    }
    @Test
    void generateRefreshTokenTest2() {
        when(userRepository.findByName("user1")).thenReturn(Optional.empty());

        try {
            final String token = jwtTokenProvider.generateRefreshToken("user1");
        } catch (Exception e) {
            assertEquals("incorrect result", "No value present", e.getMessage());
            return;
        }

        fail("incorrect result");
    }

    @Test
    void getIdTest1() {
        assertEquals("incorrect result", user.getUuid(), jwtTokenProvider.getId(token));
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
        assertEquals("incorrect result", user.getName(), jwtTokenProvider.getUsername(token));
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
            user.getRole().getPermissions().stream().map(Permission::getName).collect(Collectors.toSet()),
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

    @Test
    void validateRefreshTokenTest1() {
        assertEquals(
            "incorrect result",
            true,
            jwtTokenProvider.validateRefreshToken(refreshToken)
        );
    }
    @Test
    void validateRefreshTokenTest2() {
        try {
            jwtTokenProvider.validateRefreshToken(incorrectRefreshToken);
        } catch (Exception e) {
            assertEquals("incorrect result", "JWT expired", e.getMessage().substring(0, 11));
            return;
        }

        fail("incorrect result");
    }

    @Test
    void getRefreshIdTest1() {
        assertEquals(
            "incorrect result",
            user.getUuid().toString(),
            jwtTokenProvider.getRefreshId(refreshToken)
        );
    }
    @Test
    void getRefreshIdTest2() {
        try {
            jwtTokenProvider.getRefreshId(incorrectRefreshToken);
        } catch (Exception e) {
            assertEquals("incorrect result", "JWT expired", e.getMessage().substring(0, 11));
            return;
        }

        fail("incorrect result");
    }
}
