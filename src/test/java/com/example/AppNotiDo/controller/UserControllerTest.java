package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.UserProfileUpdateRequest;
import com.example.AppNotiDo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Si tu as un filtre JWT, il faudra éventuellement le mocker ici aussi

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCurrentUserProfile_ReturnsUserDTO() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setDisplayName("Alice Display");

        when(userService.getUserByUsername("alice")).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/users/profile")
                        .principal(() -> "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.displayName", is("Alice Display")));
    }

    @Test
    void updateCurrentUserDisplayName_UpdatesDisplayName() throws Exception {
        // Arrange
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("alice");
        existing.setEmail("alice@example.com");
        existing.setDisplayName("Old Name");

        User updated = new User();
        updated.setId(1L);
        updated.setUsername("alice");
        updated.setEmail("alice@example.com");
        updated.setDisplayName("New Name");

        when(userService.getUserByUsername("alice")).thenReturn(existing);
        when(userService.saveUser(any(User.class))).thenReturn(updated);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setDisplayName("New Name");

        // Act & Assert
        mockMvc.perform(patch("/api/users/profile")
                        .principal(() -> "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName", is("New Name")));
    }
}
