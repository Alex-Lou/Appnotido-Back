package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.UserDTO;
import com.example.AppNotiDo.dto.UserProfileUpdateRequest;
import com.example.AppNotiDo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void getCurrentUserProfile_ReturnsUserDTO() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("alice");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setDisplayName("Alice Display");

        when(userService.getUserByUsername("alice")).thenReturn(user);

        // Act
        UserDTO result = userController.getCurrentUserProfile(authentication);

        // Assert
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertEquals("Alice Display", result.getDisplayName());
    }

    @Test
    void updateCurrentUserDisplayName_UpdatesDisplayName() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("alice");

        User existing = new User();
        existing.setId(1L);
        existing.setUsername("alice");
        existing.setEmail("alice@example.com");
        existing.setDisplayName("Old Name");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("alice");
        saved.setEmail("alice@example.com");
        saved.setDisplayName("New Name");

        when(userService.getUserByUsername("alice")).thenReturn(existing);
        when(userService.saveUser(any(User.class))).thenReturn(saved);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setDisplayName("New Name");

        // Act
        UserDTO result = userController.updateCurrentUserDisplayName(request, authentication);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getDisplayName());

        // Vérifie qu'on a bien appelé le service avec le bon user
        verify(userService, times(1)).getUserByUsername("alice");
        verify(userService, times(1)).saveUser(existing);
        assertEquals("New Name", existing.getDisplayName());
    }
}
