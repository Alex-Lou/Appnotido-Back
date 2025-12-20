package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.UserNotFoundException;
import com.example.AppNotiDo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("alice");
        testUser.setEmail("alice@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole("ROLE_USER");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(testUser);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("alice", result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
    }

    @Test
    void getUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("alice");

        // Assert
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
    }
}