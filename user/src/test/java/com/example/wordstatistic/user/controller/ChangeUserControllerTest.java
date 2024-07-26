package com.example.wordstatistic.user.controller;

import com.example.wordstatistic.user.dto.ChangeUserPasswordDTO;
import com.example.wordstatistic.user.dto.ChangeUsernameDTO;
import com.example.wordstatistic.user.dto.DeleteUserDTO;
import com.example.wordstatistic.user.model.Permission;
import com.example.wordstatistic.user.model.Role;
import com.example.wordstatistic.user.model.User;
import com.example.wordstatistic.user.repository.UserRepository;
import com.example.wordstatistic.user.security.JwtTokenProvider;
import com.example.wordstatistic.user.service.ChangeUserService;
import com.example.wordstatistic.user.util.RestApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChangeUserControllerTest {
    @MockBean
    private final ChangeUserService changeUserService;
    @MockBean
    private final JwtTokenProvider jwtTokenProvider;
    @MockBean
    private final UserRepository userRepository;

    @LocalServerPort
    private Integer port;
    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @AfterAll
    static void afterAll() { }
    @BeforeAll
    static void beforeAll() { }

    private UUID userId;
    private String username;
    private Set<String> setPermissions;
    private String validToken;
    private String invalidToken;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        username = "user1";
        setPermissions = Set.of("per1", "per2", "per3");

        validToken = "validToken";
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getPermissions(validToken)).thenReturn(setPermissions);
        when(jwtTokenProvider.getUsername(validToken)).thenReturn(username);
        when(jwtTokenProvider.getId(validToken)).thenReturn(userId);
        when(userRepository.findByUuid(userId)).thenReturn(Optional.of(
            new User(
                1,
                userId, username,
                "passworduser1",
                new Role(
                    1,
                    "role1",
                    setPermissions.stream().map(e -> new Permission(null, e)).collect(Collectors.toSet())
                )
            )
        ));
        when(userRepository.findByName(username)).thenReturn(Optional.of(
            new User(
                1,
                userId, username,
                "passworduser1",
                new Role(
                    1,
                    "role1",
                    setPermissions.stream().map(e -> new Permission(null, e)).collect(Collectors.toSet())
                )
            )
        ));

        invalidToken = "invalidToken";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        //when(jwtTokenProvider.getPermissions(invalidToken)).thenThrow();
        //when(jwtTokenProvider.getUsername(invalidToken)).thenThrow();
        //when(jwtTokenProvider.getId(invalidToken)).thenThrow();
    }
    @AfterEach
    void tearDown() { }

    @Autowired
    public ChangeUserControllerTest(
        final ChangeUserService changeUserService,
        final JwtTokenProvider jwtTokenProvider,
        final UserRepository userRepository
    ) {
        this.changeUserService = changeUserService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Test
    public void changePasswordTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newPasswordCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(changeUserService)
            .changePassword(
                userCap.capture(), currentPasswordCap.capture(), newPasswordCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        ChangeUserPasswordDTO requestObject = new ChangeUserPasswordDTO("p1", "p2");
        HttpEntity<ChangeUserPasswordDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/changePassword",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p1",
            currentPasswordCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p2",
            newPasswordCap.getValue()
        );
    }
    @Test
    public void changePasswordTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newPasswordCap = ArgumentCaptor.forClass(String.class);
        doThrow(ChangeUserService.USER_NOT_FOUND_EXCEPTION).when(changeUserService)
            .changePassword(
                userCap.capture(), currentPasswordCap.capture(), newPasswordCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        ChangeUserPasswordDTO requestObject = new ChangeUserPasswordDTO("p1", "p2");
        HttpEntity<ChangeUserPasswordDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/changePassword",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p1",
            currentPasswordCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p2",
            newPasswordCap.getValue()
        );
    }
    @Test
    public void changePasswordTest3() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newPasswordCap = ArgumentCaptor.forClass(String.class);
        doThrow(ChangeUserService.USER_NOT_FOUND_EXCEPTION).when(changeUserService)
            .changePassword(
                userCap.capture(), currentPasswordCap.capture(), newPasswordCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ChangeUserPasswordDTO requestObject = new ChangeUserPasswordDTO("p1", "p2");
        HttpEntity<ChangeUserPasswordDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/changePassword",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 0, userCap.getAllValues().size());
    }

    @Test
    public void changeUsernameTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newUsernameCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(changeUserService)
            .changeUsername(
                userCap.capture(), currentPasswordCap.capture(), newUsernameCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        ChangeUsernameDTO requestObject = new ChangeUsernameDTO("p1", "user1new");
        HttpEntity<ChangeUsernameDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/changeUsername",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p1",
            currentPasswordCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "user1new",
            newUsernameCap.getValue()
        );
    }
    @Test
    public void changeUsernameTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newUsernameCap = ArgumentCaptor.forClass(String.class);
        doThrow(ChangeUserService.USER_NOT_FOUND_EXCEPTION).when(changeUserService)
            .changeUsername(
                userCap.capture(), currentPasswordCap.capture(), newUsernameCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        ChangeUsernameDTO requestObject = new ChangeUsernameDTO("p1", "user1new");
        HttpEntity<ChangeUsernameDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/changeUsername",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p1",
            currentPasswordCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "user1new",
            newUsernameCap.getValue()
        );
    }
    @Test
    public void changeUsernameTest3() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> newUsernameCap = ArgumentCaptor.forClass(String.class);
        doThrow(ChangeUserService.USER_NOT_FOUND_EXCEPTION).when(changeUserService)
            .changeUsername(
                userCap.capture(), currentPasswordCap.capture(), newUsernameCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        ChangeUsernameDTO requestObject = new ChangeUsernameDTO("p1", "user1new");
        HttpEntity<ChangeUsernameDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/changeUsername",
                HttpMethod.PUT, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 0, userCap.getAllValues().size());
    }

    @Test
    public void deleteUserTest1() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        doNothing().when(changeUserService)
            .deleteUser(
                userCap.capture(), currentPasswordCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        DeleteUserDTO requestObject = new DeleteUserDTO("p1");
        HttpEntity<DeleteUserDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/deleteUser",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.OK, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p1",
            currentPasswordCap.getValue()
        );
    }
    @Test
    public void deleteUserTest2() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        doThrow(ChangeUserService.USER_NOT_FOUND_EXCEPTION).when(changeUserService)
            .deleteUser(
                userCap.capture(), currentPasswordCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        DeleteUserDTO requestObject = new DeleteUserDTO("p1");
        HttpEntity<DeleteUserDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/deleteUser",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 1, userCap.getAllValues().size());
        assertEquals(
            "incorrect result",
            userId,
            userCap.getValue()
        );
        assertEquals(
            "incorrect result",
            "p1",
            currentPasswordCap.getValue()
        );
    }
    @Test
    public void deleteUserTest3() throws RestApiException {
        ArgumentCaptor<UUID> userCap = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> currentPasswordCap = ArgumentCaptor.forClass(String.class);
        doThrow(ChangeUserService.USER_NOT_FOUND_EXCEPTION).when(changeUserService)
            .deleteUser(
                userCap.capture(), currentPasswordCap.capture()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(invalidToken);
        DeleteUserDTO requestObject = new DeleteUserDTO("p1");
        HttpEntity<DeleteUserDTO> requestEntity = new HttpEntity<>(requestObject, headers);
        ResponseEntity<String> response = testRestTemplate
            .exchange("http://localhost:"+this.port+"/changeUser/deleteUser",
                HttpMethod.DELETE, requestEntity, String.class);
        assertEquals("incorrect result", HttpStatus.UNAUTHORIZED, response.getStatusCode());

        String res = response.getBody();
        assertEquals(
            "incorrect result",
            true,
            res == null
        );
        assertEquals("incorrect result", 0, userCap.getAllValues().size());
    }
}
