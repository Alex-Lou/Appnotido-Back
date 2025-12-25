package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.TaskNotFoundException;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public TaskService(TaskRepository taskRepository, SecurityUtils securityUtils) {
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    public Task createTask(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        User currentUser = securityUtils.getCurrentUser();
        task.setUser(currentUser);

        return taskRepository.save(task);
    }

    public Page<Task> getAllTasks(int page, int size) {
        User currentUser = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByUserId(currentUser.getId(), pageable);
    }

    public Task getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        checkTaskOwnership(task);
        return task;
    }

    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        if (updatedTask.getTitle() != null) {
            existingTask.setTitle(updatedTask.getTitle());
        }
        if (updatedTask.getDescription() != null) {
            existingTask.setDescription(updatedTask.getDescription());
        }
        if (updatedTask.getStatus() != null) {
            existingTask.setStatus(updatedTask.getStatus());
        }
        if (updatedTask.getPriority() != null) {
            existingTask.setPriority(updatedTask.getPriority());
        }
        if (updatedTask.getDueDate() != null) {
            existingTask.setDueDate(updatedTask.getDueDate());
        }
        if (updatedTask.getEstimatedDuration() != null) {
            existingTask.setEstimatedDuration(updatedTask.getEstimatedDuration());
        }
        if (updatedTask.getReminderMinutes() != null) {
            existingTask.setReminderMinutes(updatedTask.getReminderMinutes());
        }
        if (updatedTask.getNotified() != null) {
            existingTask.setNotified(updatedTask.getNotified());
        }
        if (updatedTask.getLocked() != null) {
            existingTask.setLocked(updatedTask.getLocked());
        }

        if (updatedTask.getTags() != null) {
            existingTask.setTags(updatedTask.getTags());
        }

        if (updatedTask.getIsRunning() != null) {
            existingTask.setIsRunning(updatedTask.getIsRunning());
        }
        if (updatedTask.getStartedAt() != null) {
            existingTask.setStartedAt(updatedTask.getStartedAt());
        }
        if (updatedTask.getPausedAt() != null) {
            existingTask.setPausedAt(updatedTask.getPausedAt());
        }
        if (updatedTask.getTimeSpent() != null) {
            existingTask.setTimeSpent(updatedTask.getTimeSpent());
        }

        return taskRepository.save(existingTask);
    }

    public List<Task> getTaskByStatus(TaskStatus status) {
        User currentUser = securityUtils.getCurrentUser();
        return taskRepository.findByStatusAndUserId(status, currentUser.getId());
    }

    public List<Task> getTaskByPriority(TaskPriority priority) {
        User currentUser = securityUtils.getCurrentUser();
        return taskRepository.findByPriorityAndUserId(priority, currentUser.getId());
    }

    public List<Task> getTaskByStatusAndPriority(TaskStatus status, TaskPriority priority) {
        User currentUser = securityUtils.getCurrentUser();
        return taskRepository.findByStatusAndPriorityAndUserId(status, priority, currentUser.getId());
    }

    public Page<Task> getTasksByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByUserId(userId, pageable);
    }

    private void checkTaskOwnership(Task task) {
        User currentUser = securityUtils.getCurrentUser();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You don't have permission to access this task");
        }
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    // ======================
    //        TIMER
    // ======================

    @Transactional
    public Task startTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your task");
        }

        if (Boolean.TRUE.equals(task.getIsRunning())) {
            return task;
        }

        task.setStartedAt(LocalDateTime.now());
        task.setPausedAt(null);
        task.setIsRunning(true);

        return taskRepository.save(task);
    }

    @Transactional
    public Task pauseTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your task");
        }

        if (!Boolean.TRUE.equals(task.getIsRunning()) || task.getStartedAt() == null) {
            return task;
        }

        LocalDateTime now = LocalDateTime.now();
        // ✅ CHANGEMENT : toSeconds() au lieu de toMinutes()
        long seconds = Duration.between(task.getStartedAt(), now).toSeconds();

        int current = task.getTimeSpent() != null ? task.getTimeSpent() : 0;
        task.setTimeSpent(current + (int) seconds);

        task.setPausedAt(now);
        task.setIsRunning(false);

        return taskRepository.save(task);
    }

    @Transactional
    public Task stopTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your task");
        }

        // ✅ Si le timer tourne encore, sauvegarder le temps final
        if (Boolean.TRUE.equals(task.getIsRunning()) && task.getStartedAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            // ✅ CHANGEMENT : toSeconds() au lieu de toMinutes()
            long seconds = Duration.between(task.getStartedAt(), now).toSeconds();

            int current = task.getTimeSpent() != null ? task.getTimeSpent() : 0;
            task.setTimeSpent(current + (int) seconds);

            task.setPausedAt(now);
        }

        task.setIsRunning(false);
        task.setStatus(TaskStatus.DONE);

        return taskRepository.save(task);
    }
}
