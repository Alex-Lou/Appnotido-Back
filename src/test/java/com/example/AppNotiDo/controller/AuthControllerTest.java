package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.LoginRequest;
import com.example.AppNotiDo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Nettoyer la DB avant chaque test
        userRepository.deleteAll();

        // Créer un user de test
        testUser = new User();
        testUser.setUsername("alice");
        testUser.setEmail("alice@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("ROLE_USER");
        testUser.setTheme("light");
        userRepository.save(testUser);
    }

    @Test
    void register_Success() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setUsername("bob");
        newUser.setEmail("bob@example.com");
        newUser.setPassword("password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().httpOnly("token", true))
                .andReturn();

        // Vérifier que le cookie existe
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotEmpty();
        assertThat(cookies[0].getName()).isEqualTo("token");
        assertThat(cookies[0].isHttpOnly()).isTrue();

        // Vérifier que l'user est bien en DB
        User savedUser = userRepository.findByUsername("bob").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("bob@example.com");
        assertThat(savedUser.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void register_UsernameAlreadyExists_Returns409() throws Exception {
        // Arrange
        User duplicateUser = new User();
        duplicateUser.setUsername("alice"); // Déjà existant
        duplicateUser.setEmail("newemail@example.com");
        duplicateUser.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().httpOnly("token", true))
                .andReturn();

        // Vérifier le cookie
        Cookie tokenCookie = result.getResponse().getCookie("token");
        assertThat(tokenCookie).isNotNull();
        assertThat(tokenCookie.getValue()).isNotEmpty();
        assertThat(tokenCookie.isHttpOnly()).isTrue();
        assertThat(tokenCookie.getMaxAge()).isEqualTo(7 * 24 * 3600); // 7 jours
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("token"));
    }

    @Test
    void login_UserNotFound_Returns401() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_Success() throws Exception {
        // Arrange - D'abord se connecter
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        Cookie tokenCookie = loginResult.getResponse().getCookie("token");

        // Act - Se déconnecter
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andReturn();

        // Assert - Le cookie doit être expiré
        Cookie expiredCookie = logoutResult.getResponse().getCookie("token");
        assertThat(expiredCookie).isNotNull();
        assertThat(expiredCookie.getMaxAge()).isEqualTo(0); // Expiré
    }
}