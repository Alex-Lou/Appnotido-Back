
package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.UserDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toDTO_MapsAllFields_WithDisplayName() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setTheme("dark");
        user.setDisplayName("Alice Display");

        // Act
        UserDTO dto = UserMapper.toDTO(user);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("dark", dto.getTheme());
        assertEquals("Alice Display", dto.getDisplayName());
    }

    @Test
    void toDTO_FallbackToUsername_WhenDisplayNameNullOrEmpty() {
        // Arrange
        User userNull = new User();
        userNull.setUsername("bob");
        userNull.setDisplayName(null);

        User userEmpty = new User();
        userEmpty.setUsername("charlie");
        userEmpty.setDisplayName("   ");

        // Act
        UserDTO dtoNull = UserMapper.toDTO(userNull);
        UserDTO dtoEmpty = UserMapper.toDTO(userEmpty);

        // Assert
        assertEquals("bob", dtoNull.getDisplayName());
        assertEquals("charlie", dtoEmpty.getDisplayName());
    }

    @Test
    void toDTO_TaskCount_UsesTasksSizeOrZero() {
        // Arrange
        User userWithTasks = new User();
        userWithTasks.setUsername("dora");
        userWithTasks.getTasks().add(null);
        userWithTasks.getTasks().add(null);

        User userNoTasks = new User();
        userNoTasks.setUsername("eva");
        userNoTasks.setTasks(null);

        // Act
        UserDTO dtoWithTasks = UserMapper.toDTO(userWithTasks);
        UserDTO dtoNoTasks = UserMapper.toDTO(userNoTasks);

        // Assert
        assertEquals(2, dtoWithTasks.getTaskCount());
        assertEquals(0, dtoNoTasks.getTaskCount());
    }
}
