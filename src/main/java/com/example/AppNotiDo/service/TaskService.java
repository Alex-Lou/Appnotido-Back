package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Project;
import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.TaskNotFoundException;
import com.example.AppNotiDo.repository.ProjectRepository;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, SecurityUtils securityUtils, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
        this.notificationService = notificationService;
    }

    @Transactional
    public Task createTask(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        User currentUser = securityUtils.getCurrentUser();
        task.setUser(currentUser);

        Task savedTask = taskRepository.save(task);

        // 📝 Notification de création
        notificationService.notifyTaskCreated(savedTask);

        return savedTask;
    }

    @Transactional
    public Task createTaskWithProject(Task task, Long projectId) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        User currentUser = securityUtils.getCurrentUser();
        task.setUser(currentUser);

        // Assigner le projet si spécifié
        if (projectId != null) {
            Project project = projectRepository.findByIdAndUserId(projectId, currentUser.getId())
                    .orElse(null);
            task.setProject(project);
        }

        Task savedTask = taskRepository.save(task);

        // 📝 Notification de création
        notificationService.notifyTaskCreated(savedTask);

        return savedTask;
    }

    public Page<Task> getAllTasks(int page, int size) {
        User currentUser = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByUserId(currentUser.getId(), pageable);
    }

    public Page<Task> getTasksByProject(Long projectId, int page, int size) {
        User currentUser = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByProjectIdAndUserId(projectId, currentUser.getId(), pageable);
    }

    public Task getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        checkTaskOwnership(task);
        return task;
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        String taskTitle = task.getTitle();
        User user = task.getUser();

        // Supprimer les notifications liées à cette tâche
        notificationService.deleteNotificationsByTask(id);

        taskRepository.deleteById(id);

        // 🗑️ Notification de suppression
        notificationService.notifyTaskDeleted(user, taskTitle);
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        // Liste des modifications pour le message de notification
        List<String> changes = new ArrayList<>();

        // ========================================
        // DÉTECTER LES CHANGEMENTS AVANT MODIFICATION
        // ========================================

        // Titre
        boolean titleChanged = updatedTask.getTitle() != null &&
                !Objects.equals(updatedTask.getTitle(), existingTask.getTitle());
        if (titleChanged) changes.add("titre");

        // Description
        boolean descriptionChanged = updatedTask.getDescription() != null &&
                !Objects.equals(updatedTask.getDescription(), existingTask.getDescription());
        if (descriptionChanged) changes.add("description");

        // Statut
        String oldStatus = existingTask.getStatus() != null ? existingTask.getStatus().name() : null;
        String newStatus = updatedTask.getStatus() != null ? updatedTask.getStatus().name() : oldStatus;
        boolean statusChanged = !Objects.equals(oldStatus, newStatus);

        // Priorité
        String oldPriority = existingTask.getPriority() != null ? existingTask.getPriority().name() : null;
        String newPriority = updatedTask.getPriority() != null ? updatedTask.getPriority().name() : oldPriority;
        boolean priorityChanged = !Objects.equals(oldPriority, newPriority);

        // Date d'échéance
        LocalDateTime oldDueDate = existingTask.getDueDate();
        boolean dueDateChanged = updatedTask.getDueDate() != null &&
                !Objects.equals(updatedTask.getDueDate(), existingTask.getDueDate());
        if (dueDateChanged) changes.add("échéance");

        // Durée estimée
        boolean durationChanged = updatedTask.getEstimatedDuration() != null &&
                !Objects.equals(updatedTask.getEstimatedDuration(), existingTask.getEstimatedDuration());
        if (durationChanged) changes.add("durée estimée");

        // Rappel
        boolean reminderChanged = updatedTask.getReminderMinutes() != null &&
                !Objects.equals(updatedTask.getReminderMinutes(), existingTask.getReminderMinutes());
        if (reminderChanged) changes.add("rappel");

        // Tags
        boolean tagsChanged = updatedTask.getTags() != null &&
                !Objects.equals(updatedTask.getTags(), existingTask.getTags());
        if (tagsChanged) changes.add("tags");

        // Réactivable
        Boolean wasReactivable = existingTask.getReactivable();
        boolean reactivableChanged = updatedTask.getReactivable() != null &&
                !Objects.equals(updatedTask.getReactivable(), existingTask.getReactivable());
        if (reactivableChanged) changes.add("réactivable");

        // Timer
        boolean timerChanged = updatedTask.getTimerEnabled() != null &&
                !Objects.equals(updatedTask.getTimerEnabled(), existingTask.getTimerEnabled());
        if (timerChanged) changes.add("timer");

        // Projet
        boolean projectChanged = !Objects.equals(
                updatedTask.getProject() != null ? updatedTask.getProject().getId() : null,
                existingTask.getProject() != null ? existingTask.getProject().getId() : null
        );
        if (projectChanged) changes.add("projet");

        // ========================================
        // APPLIQUER LES MODIFICATIONS
        // ========================================

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
        if (updatedTask.getReactivable() != null) {
            existingTask.setReactivable(updatedTask.getReactivable());
        }
        if (updatedTask.getTimerEnabled() != null) {
            existingTask.setTimerEnabled(updatedTask.getTimerEnabled());
        }
        // Projet - on permet de mettre à null pour retirer du projet
        if (projectChanged) {
            existingTask.setProject(updatedTask.getProject());
        }

        Task savedTask = taskRepository.save(existingTask);

        // ========================================
        // CRÉER LES NOTIFICATIONS
        // ========================================

        // 📊 Changement de statut
        if (statusChanged) {
            if ("DONE".equals(newStatus)) {
                notificationService.notifyTaskCompleted(savedTask);
            } else {
                notificationService.notifyStatusChanged(savedTask, oldStatus, newStatus);
            }
        }

        // 🎯 Changement de priorité
        if (priorityChanged) {
            notificationService.notifyPriorityChanged(savedTask, oldPriority, newPriority);
        }

        // 🔄 Tâche réactivée (déplacée à aujourd'hui)
        if (Boolean.TRUE.equals(wasReactivable) && dueDateChanged && updatedTask.getDueDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime newDueDateValue = updatedTask.getDueDate();

            if (oldDueDate != null && !oldDueDate.toLocalDate().equals(newDueDateValue.toLocalDate())) {
                if (newDueDateValue.toLocalDate().equals(now.toLocalDate())) {
                    notificationService.notifyTaskReactivated(savedTask);
                    // Retirer "échéance" des changes car on a déjà notifié pour réactivation
                    changes.remove("échéance");
                }
            }
        }

        // ✏️ Notification générique pour autres modifications
        if (!statusChanged && !priorityChanged && !changes.isEmpty()) {
            String detail = String.join(", ", changes) + " modifié" + (changes.size() > 1 ? "s" : "");
            notificationService.notifyTaskUpdated(savedTask, detail);
        }

        return savedTask;
    }

    @Transactional
    public Task updateTaskWithProject(Long id, Task updatedTask, Long projectId) {
        User currentUser = securityUtils.getCurrentUser();

        // Charger le projet si spécifié
        if (projectId != null) {
            Project project = projectRepository.findByIdAndUserId(projectId, currentUser.getId())
                    .orElse(null);
            updatedTask.setProject(project);
        } else {
            updatedTask.setProject(null);
        }

        return updateTask(id, updatedTask);
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

        Task savedTask = taskRepository.save(task);

        // ▶️ Notification timer démarré
        notificationService.notifyTimerStarted(savedTask);

        return savedTask;
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
        long seconds = Duration.between(task.getStartedAt(), now).toSeconds();

        int current = task.getTimeSpent() != null ? task.getTimeSpent() : 0;
        int newTimeSpent = current + (int) seconds;
        task.setTimeSpent(newTimeSpent);

        task.setPausedAt(now);
        task.setIsRunning(false);

        Task savedTask = taskRepository.save(task);

        // ⏸️ Notification timer en pause
        notificationService.notifyTimerPaused(savedTask, newTimeSpent);

        return savedTask;
    }

    @Transactional
    public Task stopTask(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your task");
        }

        int totalTimeSpent = task.getTimeSpent() != null ? task.getTimeSpent() : 0;

        if (Boolean.TRUE.equals(task.getIsRunning()) && task.getStartedAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            long seconds = Duration.between(task.getStartedAt(), now).toSeconds();
            totalTimeSpent += (int) seconds;
            task.setTimeSpent(totalTimeSpent);
            task.setPausedAt(now);
        }

        task.setIsRunning(false);
        task.setStatus(TaskStatus.DONE);
        task.setTimerEnabled(false);

        Task savedTask = taskRepository.save(task);

        // ⏹️ Notification timer arrêté (inclut le temps total)
        notificationService.notifyTimerStopped(savedTask, totalTimeSpent);

        return savedTask;
    }
}