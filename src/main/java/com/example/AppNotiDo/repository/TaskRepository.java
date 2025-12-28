package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusAndUserId(TaskStatus status, Long userId);

    List<Task> findByPriorityAndUserId(TaskPriority priority, Long userId);

    List<Task> findByStatusAndPriorityAndUserId(TaskStatus status, TaskPriority priority, Long userId);

    Page<Task> findByUserId(Long userId, Pageable pageable);

    List<Task> findByUserAndDueDateBetweenAndStatus(User user, LocalDateTime start, LocalDateTime end, TaskStatus status);

    List<Task> findByUserAndDueDateBetweenAndStatusNot(
            User user,
            LocalDateTime start,
            LocalDateTime end,
            TaskStatus status
    );

    List<Task> findByUserAndDueDateBetweenAndStatusNotAndReactivableTrue(
            User user,
            LocalDateTime start,
            LocalDateTime end,
            TaskStatus excludedStatus
    );

    List<Task> findByUserAndDueDateBeforeAndStatusNotAndReactivableTrue(
            User user,
            LocalDateTime now,
            TaskStatus status
    );

    // ========================================
    // MÉTHODES POUR LES NOTIFICATIONS
    // ========================================

    // Récupérer les tâches non terminées qui ont une date d'échéance
    List<Task> findByStatusNotAndDueDateIsNotNull(TaskStatus status);

    // Récupérer les tâches non notifiées avec échéance entre deux dates
    List<Task> findByStatusNotAndDueDateBetweenAndNotifiedFalse(
            TaskStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    // ===== AJOUTER CES MÉTHODES DANS TaskRepository.java =====

    // Trouver les tâches d'un projet
    List<Task> findByProjectId(Long projectId);

    // Trouver les tâches d'un projet avec pagination
    Page<Task> findByProjectIdAndUserId(Long projectId, Long userId, Pageable pageable);

    // Supprimer les tâches d'un projet
    void deleteByProjectId(Long projectId);

    // Trouver les tâches sans projet (inbox)
    Page<Task> findByUserIdAndProjectIsNull(Long userId, Pageable pageable);

    // Compter les tâches d'un projet
    long countByProjectId(Long projectId);

    // Compter les tâches terminées d'un projet
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    // Trouver une tâche par ID et userId (sécurité)
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    // Trouver les templates de récurrence dont la prochaine occurrence est avant une date
    List<Task> findByIsRecurringTemplateTrueAndNextOccurrenceBefore(LocalDateTime date);

    // Trouver les templates de récurrence d'un utilisateur
    List<Task> findByUserIdAndIsRecurringTemplateTrue(Long userId);
}