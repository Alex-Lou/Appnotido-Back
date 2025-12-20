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
        // Valeurs par défaut
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        // Associer automatiquement au user connecté
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

        // Modifier seulement si le champ n'est pas null
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
        // IMPORTANT : Ajouter la mise à jour du locked
        if (updatedTask.getLocked() != null) {
            existingTask.setLocked(updatedTask.getLocked());
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
}