package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.TaskNotFoundException;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityUtils securityUtils;

    private TaskService taskService;

    private Task testTask;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Créer manuellement TaskService avec les mocks
        taskService = new TaskService(taskRepository, securityUtils);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("alice");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setUser(testUser);

        // Mock SecurityUtils pour retourner testUser
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    void createTask_ShouldSetDefaultValues() {
        // Arrange
        Task newTask = new Task();
        newTask.setTitle("Titre du test");
        newTask.setDescription("Description de la task test");

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(1L);
            return savedTask;
        });

        // Act
        Task result = taskService.createTask(newTask);

        // Assert
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(result.getTitle()).isEqualTo("Titre du test");
        assertThat(result.getUser()).isEqualTo(testUser);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskById_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        Task result = taskService.getTaskById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with id: 999");

        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void getAllTasks_ShouldReturnPagedTasks() {
        // Arrange
        Task task1 = new Task();
        task1.setTitle("Tâche 1");
        task1.setUser(testUser);

        Task task2 = new Task();
        task2.setTitle("Tâche 2");
        task2.setUser(testUser);

        Task task3 = new Task();
        task3.setTitle("Tâche 3");
        task3.setUser(testUser);

        List<Task> tasks = List.of(task1, task2, task3);
        Page<Task> taskPage = new PageImpl<>(tasks);

        when(taskRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(taskPage);

        // Act
        Page<Task> result = taskService.getAllTasks(0, 10);

        // Assert
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        verify(taskRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void updateTask_Success() {
        // Arrange
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(TaskPriority.HIGH);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        Task result = taskService.updateTask(1L, updatedTask);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void updateTask_NotFound_ThrowsException() {
        // Arrange
        Task updatedTask = new Task();
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.updateTask(999L, updatedTask))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).deleteById(1L);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_NotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(999L))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void checkTaskOwnership_DifferentUser_ThrowsException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("bob");

        Task otherTask = new Task();
        otherTask.setId(2L);
        otherTask.setUser(otherUser);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(otherTask));

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You don't have permission");
    }
}