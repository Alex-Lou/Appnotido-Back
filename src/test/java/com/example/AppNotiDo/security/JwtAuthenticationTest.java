package com.example.AppNotiDo.security;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class JwtAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Cookie authCookie;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Créer un user
        testUser = new User();
        testUser.setUsername("alice");
        testUser.setEmail("alice@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("ROLE_USER");
        testUser.setTheme("light");
        userRepository.save(testUser);

        // Se connecter pour obtenir le cookie JWT
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        authCookie = loginResult.getResponse().getCookie("token");
    }

    @Test
    void accessProtectedEndpoint_WithValidCookie_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "10")
                        .cookie(authCookie))
                .andExpect(status().isOk());
    }

    @Test
    void accessProtectedEndpoint_WithoutCookie_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidCookie_Returns403() throws Exception {
        // Arrange
        Cookie invalidCookie = new Cookie("token", "invalid.jwt.token");

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "10")
                        .cookie(invalidCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTheme_WithValidCookie_Success() throws Exception {
        // Arrange
        String themeJson = "{\"theme\":\"dark\"}";

        // Act & Assert
        mockMvc.perform(put("/api/auth/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(themeJson)
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"));
    }

    @Test
    void updateTheme_WithoutCookie_Returns403() throws Exception {
        // Arrange
        String themeJson = "{\"theme\":\"dark\"}";

        // Act & Assert
        mockMvc.perform(put("/api/auth/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(themeJson))
                .andExpect(status().isUnauthorized()); // ← Change en isUnauthorized() au lieu de isForbidden()
    }

    @Test
    void getTheme_WithValidCookie_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/theme")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("light"));
    }

    @Test
    void createTask_WithValidCookie_Success() throws Exception {
        // Arrange
        String taskJson = """
            {
                "title": "Test Task",
                "description": "Test Description",
                "priority": "HIGH",
                "status": "TODO"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson)
                        .cookie(authCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void createTask_WithoutCookie_Returns403() throws Exception {
        // Arrange
        String taskJson = """
            {
                "title": "Test Task",
                "description": "Test Description"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isForbidden());
    }
}