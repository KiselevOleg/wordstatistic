package com.example.wordstatistic.user.service;

import com.example.wordstatistic.user.config.SecurityConfig;
import com.example.wordstatistic.user.dto.TokenDTO;
import com.example.wordstatistic.user.dto.UserDTO;
import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.Role;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.PermissionRepository;
import com.example.wordstatistic.user.repository.RoleRepository;
import com.example.wordstatistic.user.repository.UserRepository;
import com.example.wordstatistic.user.security.JwtTokenProvider;
import com.example.wordstatistic.user.util.RestApiException;
import io.jsonwebtoken.security.Password;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceTest {
    @MockBean
    private final UserRepository userRepository;
    @MockBean
    private final RoleRepository roleRepository;
    @MockBean
    private final PermissionRepository permissionRepository;
    @MockBean
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public UserServiceTest(
        final UserRepository userRepository,
        final RoleRepository roleRepository,
        final PermissionRepository permissionRepository,
        final JwtTokenProvider jwtTokenProvider,
        final UserService userService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    @BeforeEach
    void setUp() { }
    @AfterEach
    void tearDown() { }

    @Test
    public void singUpTest1() throws RestApiException {
        final Permission permission1 = new Permission(1, "permission1");
        final Permission permission2 = new Permission(2, "permission2");
        final Role role = new Role(1, "user", Set.of(permission1, permission2));

        when(userRepository.existsByName("user1")).thenReturn(false);
        when(roleRepository.findByName("user")).thenReturn(Optional.of(role));
        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCap.capture())).thenReturn(null);

        userService.singUp(new UserDTO("user1", "password1"));

        assertEquals("incorrect saving", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect saving",
            true,
            userCap.getValue().getId() == null
        );
        assertEquals(
            "incorrect saving",
            false,
            userCap.getValue().getUuid() == null
        );
        assertEquals(
            "incorrect saving",
            "user1",
            userCap.getValue().getName()
        );
        assertEquals(
            "incorrect saving",
            role,
            userCap.getValue().getRole()
        );
    }
    @Test
    public void singUpTest2() throws RestApiException {
        final Permission permission1 = new Permission(1, "permission1");
        final Permission permission2 = new Permission(2, "permission2");
        final Role role = new Role(1, "user", Set.of(permission1, permission2));

        when(userRepository.existsByName("user1")).thenReturn(true);

        try {
            userService.singUp(new UserDTO("user1", "password1"));
        } catch(RestApiException e) {
            assertEquals("incorrect exception", "user is found", e.getMessage());
            assertEquals("incorrect exception", HttpStatus.CONFLICT, e.getStatus());
            return;
        }

        fail("an excepted exception has not been dropped");
    }

    @Test
    void JwtTokenProviderTest1() {
        when(jwtTokenProvider.validateRefreshToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.validateRefreshToken("refreshToken")).thenReturn(false);

        try {
            userService.refreshTokens(new TokenDTO("accessToken", "refreshToken"));
        } catch (RestApiException e) {
            assertEquals("incorrect exception", "invalid refresh token", e.getMessage());
            assertEquals("incorrect exception", HttpStatus.BAD_REQUEST, e.getStatus());
            return;
        }

        fail("an excepted exception has not been dropped");
    }
}
