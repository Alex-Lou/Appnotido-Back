package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Subtask;
import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.repository.SubtaskRepository;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public SubtaskService(SubtaskRepository subtaskRepository, TaskRepository taskRepository, SecurityUtils securityUtils) {
        this.subtaskRepository = subtaskRepository;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    // Récupérer toutes les sous-tâches d'une tâche
    public List<Subtask> getSubtasksByTaskId(Long taskId) {
        User currentUser = securityUtils.getCurrentUser();

        // Vérifier que la tâche appartient à l'utilisateur
        Task task = taskRepository.findByIdAndUserId(taskId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        return subtaskRepository.findByTaskIdOrderByDisplayOrderAsc(taskId);
    }

    // Créer une sous-tâche
    @Transactional
    public Subtask createSubtask(Long taskId, Subtask subtask) {
        User currentUser = securityUtils.getCurrentUser();

        // Vérifier que la tâche appartient à l'utilisateur
        Task task = taskRepository.findByIdAndUserId(taskId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        // Définir l'ordre d'affichage
        Integer maxOrder = subtaskRepository.findMaxDisplayOrderByTaskId(taskId);
        subtask.setDisplayOrder(maxOrder + 1);

        // Valeurs par défaut
        subtask.setTask(task);
        subtask.setCompleted(false);

        return subtaskRepository.save(subtask);
    }

    // Mettre à jour une sous-tâche
    @Transactional
    public Subtask updateSubtask(Long subtaskId, Subtask updatedSubtask) {
        User currentUser = securityUtils.getCurrentUser();

        Subtask existingSubtask = subtaskRepository.findByIdAndTaskUserId(subtaskId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Sous-tâche non trouvée"));

        if (updatedSubtask.getTitle() != null) {
            existingSubtask.setTitle(updatedSubtask.getTitle());
        }
        if (updatedSubtask.getCompleted() != null) {
            existingSubtask.setCompleted(updatedSubtask.getCompleted());
            // Mettre à jour la date de complétion
            if (updatedSubtask.getCompleted()) {
                existingSubtask.setCompletedAt(LocalDateTime.now());
            } else {
                existingSubtask.setCompletedAt(null);
            }
        }
        if (updatedSubtask.getDisplayOrder() != null) {
            existingSubtask.setDisplayOrder(updatedSubtask.getDisplayOrder());
        }

        return subtaskRepository.save(existingSubtask);
    }

    // Toggle le statut complété d'une sous-tâche
    @Transactional
    public Subtask toggleSubtask(Long subtaskId) {
        User currentUser = securityUtils.getCurrentUser();

        Subtask subtask = subtaskRepository.findByIdAndTaskUserId(subtaskId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Sous-tâche non trouvée"));

        boolean newStatus = !Boolean.TRUE.equals(subtask.getCompleted());
        subtask.setCompleted(newStatus);

        if (newStatus) {
            subtask.setCompletedAt(LocalDateTime.now());
        } else {
            subtask.setCompletedAt(null);
        }

        return subtaskRepository.save(subtask);
    }

    // Supprimer une sous-tâche
    @Transactional
    public void deleteSubtask(Long subtaskId) {
        User currentUser = securityUtils.getCurrentUser();

        Subtask subtask = subtaskRepository.findByIdAndTaskUserId(subtaskId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Sous-tâche non trouvée"));

        subtaskRepository.delete(subtask);
    }

    // Réordonner les sous-tâches
    @Transactional
    public void reorderSubtasks(Long taskId, List<Long> subtaskIds) {
        User currentUser = securityUtils.getCurrentUser();

        // Vérifier que la tâche appartient à l'utilisateur
        taskRepository.findByIdAndUserId(taskId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        for (int i = 0; i < subtaskIds.size(); i++) {
            Long subtaskId = subtaskIds.get(i);
            subtaskRepository.findByIdAndTaskId(subtaskId, taskId)
                    .ifPresent(subtask -> {
                        subtask.setDisplayOrder(subtaskIds.indexOf(subtaskId));
                        subtaskRepository.save(subtask);
                    });
        }
    }

    // Compter les sous-tâches
    public long countSubtasks(Long taskId) {
        return subtaskRepository.countByTaskId(taskId);
    }

    // Compter les sous-tâches complétées
    public long countCompletedSubtasks(Long taskId) {
        return subtaskRepository.countByTaskIdAndCompletedTrue(taskId);
    }
}