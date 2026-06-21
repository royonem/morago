package com.roy.morago.service.auth;

import com.roy.morago.dto.auth.*;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.Availability;
import com.roy.morago.enums.TopikLevel;
import com.roy.morago.enums.UserStatus;
import com.roy.morago.exception.auth.InvalidRefreshTokenException;
import com.roy.morago.service.user.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpringBootApplicationProperties")
@Testcontainers
@Transactional
@SpringBootTest(properties = {
        "app.jwt.secret=vLp7X9mQ2sR8tY3uF5jH6kL1nB4cD8eF0gH2jK3lP5qR7tY9u"
})
public class AuthServiceIT {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

    @Autowired
    private AuthService authService;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    private RefreshTokenService refreshTokenService;

    private ClientRegisterRequest testClientRequest;
    private TranslatorRegisterRequest testTranslatorRequest;
    private LoginRequest testLoginRequest;
    private LogoutRequest testLogoutRequest;
    private String testRefreshToken;
    private User testClient;
    private User testTranslator;

    public void createTestRegisterClientRequest() {
        testClientRequest = new ClientRegisterRequest(
                "John",
                "Doe",
                "password",
                "password",
                "johndoe@email.com",
                "010-4444-4444"
        );
    }

    public void createTestRegisterTranslatorRequest() {
        testTranslatorRequest = new TranslatorRegisterRequest(
                "Sara",
                "Park",
                "password123",
                "password123",
                "sara@translator.com",
                "010-5555-6666",
                TopikLevel.LEVEL_3,
                LocalDate.of(1990, 5, 15)
        );
    }

    public void createTestLoginRequest() {
        testLoginRequest = new LoginRequest(
                "johndoe@email.com",
                "password"
        );
    }

    public void createTestLogoutRequest() {
        testLogoutRequest = new LogoutRequest(
                testRefreshToken
        );

    }

    @BeforeEach
    public void setup() {
        createTestRegisterClientRequest();
        createTestRegisterTranslatorRequest();
        createTestLoginRequest();
    }

    @Test
    public void testRegisterClient() {
        authService.registerClient(testClientRequest);
        testClient = userHelper.findUserByEmail(testClientRequest.email());
        assertThat(testClient.getAvailability()).isEqualTo(Availability.OFFLINE);
        assertTrue(testClient.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_CLIENT")));
        assertThat(testClient.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
        assertThat(testClient.getBankAccount()).isNull();
    }

    @Test
    public void testRegisterTranslator() {
        authService.registerTranslator(testTranslatorRequest);
        testTranslator = userHelper.findUserByEmail(testTranslatorRequest.email());
        assertThat(testTranslator.getAvailability()).isEqualTo(Availability.OFFLINE);
        assertTrue(testTranslator.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_TRANSLATOR")));
        assertThat(testTranslator.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
        assertThat(testTranslator.getBankAccount()).isNull();
    }

    @Test
    public void testLogin() {
        authService.registerClient(testClientRequest);
        LoginResponse response = authService.login(testLoginRequest);
        testRefreshToken = response.refreshToken();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    public void testLogout() {
        authService.registerClient(testClientRequest);
        LoginResponse response = authService.login(testLoginRequest);
        testRefreshToken = response.refreshToken();
        createTestLogoutRequest();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authService.logout(testLogoutRequest, authentication);
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(testRefreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh Token is revoked.");
    }
}
