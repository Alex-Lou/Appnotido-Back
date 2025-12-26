package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Notification;
import com.example.AppNotiDo.domain.NotificationType;
import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.NotificationDTO;
import com.example.AppNotiDo.mapper.NotificationMapper;
import com.example.AppNotiDo.repository.NotificationRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final int MAX_NOTIFICATIONS_PER_USER = 50;

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    public NotificationService(NotificationRepository notificationRepository, SecurityUtils securityUtils) {
        this.notificationRepository = notificationRepository;
        this.securityUtils = securityUtils;
    }

    // ========================================
    // MÉTHODES DE LECTURE
    // ========================================

    public List<NotificationDTO> getAllNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Long countUnread() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
    }

    // ========================================
    // MÉTHODES D'ACTION
    // ========================================

    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return NotificationMapper.toDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        notificationRepository.markAllAsReadByUserId(currentUser.getId(), LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteNotificationsByTask(Long taskId) {
        notificationRepository.deleteByTaskId(taskId);
    }

    // ========================================
    // CRÉATION DE NOTIFICATIONS - BASE
    // ========================================

    @Transactional
    public Notification createNotification(User user, Task task, String title, String message, NotificationType type) {
        Notification notification = new Notification(user, task, title, message, type);
        notification = notificationRepository.save(notification);
        cleanupOldNotifications(user.getId());
        return notification;
    }

    // ========================================
    // NOTIFICATIONS D'ÉCHÉANCE (Scheduler)
    // ========================================

    @Transactional
    public Notification createReminderNotification(Task task) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        if (notificationRepository.existsByTaskIdAndTypeAndCreatedAtAfter(task.getId(), NotificationType.REMINDER, fiveMinutesAgo)) {
            return null;
        }
        String title = "⏰ Rappel";
        String message = String.format("La tâche \"%s\" arrive à échéance bientôt !", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.REMINDER);
    }

    @Transactional
    public Notification createDeadlineNotification(Task task) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        if (notificationRepository.existsByTaskIdAndTypeAndCreatedAtAfter(task.getId(), NotificationType.DEADLINE, fiveMinutesAgo)) {
            return null;
        }
        String title = "🎯 Échéance atteinte";
        String message = String.format("La tâche \"%s\" arrive à échéance maintenant !", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.DEADLINE);
    }

    @Transactional
    public Notification createOverdueNotification(Task task) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        if (notificationRepository.existsByTaskIdAndTypeAndCreatedAtAfter(task.getId(), NotificationType.OVERDUE, oneHourAgo)) {
            return null;
        }
        String title = "⚠️ Tâche en retard";
        String message = String.format("La tâche \"%s\" est en retard !", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.OVERDUE);
    }

    // ========================================
    // NOTIFICATIONS DE TÂCHES (CRUD)
    // ========================================

    @Transactional
    public Notification notifyTaskCreated(Task task) {
        String title = "📝 Nouvelle tâche";
        String message = String.format("Tâche \"%s\" créée avec succès.", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.TASK_CREATED);
    }

    @Transactional
    public Notification notifyTaskUpdated(Task task, String detail) {
        String title = "✏️ Tâche modifiée";
        String message = String.format("Tâche \"%s\" : %s", task.getTitle(), detail);
        return createNotification(task.getUser(), task, title, message, NotificationType.TASK_UPDATED);
    }

    @Transactional
    public Notification notifyTaskDeleted(User user, String taskTitle) {
        String title = "🗑️ Tâche supprimée";
        String message = String.format("La tâche \"%s\" a été supprimée.", taskTitle);
        return createNotification(user, null, title, message, NotificationType.TASK_DELETED);
    }

    @Transactional
    public Notification notifyTaskCompleted(Task task) {
        String title = "✅ Tâche terminée";
        String message = String.format("Bravo ! La tâche \"%s\" est terminée.", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.TASK_COMPLETED);
    }

    @Transactional
    public Notification notifyTaskReactivated(Task task) {
        String title = "🔄 Tâche réactivée";
        String message = String.format("La tâche \"%s\" a été déplacée à aujourd'hui.", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.TASK_REACTIVATED);
    }

    // ========================================
    // NOTIFICATIONS DE TIMER
    // ========================================

    @Transactional
    public Notification notifyTimerStarted(Task task) {
        String title = "▶️ Timer démarré";
        String message = String.format("Le chronomètre de \"%s\" a démarré.", task.getTitle());
        return createNotification(task.getUser(), task, title, message, NotificationType.TIMER_STARTED);
    }

    @Transactional
    public Notification notifyTimerPaused(Task task, int timeSpentSeconds) {
        String title = "⏸️ Timer en pause";
        String timeFormatted = formatTimeSpent(timeSpentSeconds);
        String message = String.format("Le chronomètre de \"%s\" est en pause. Temps : %s", task.getTitle(), timeFormatted);
        return createNotification(task.getUser(), task, title, message, NotificationType.TIMER_PAUSED);
    }

    @Transactional
    public Notification notifyTimerStopped(Task task, int totalTimeSeconds) {
        String title = "⏹️ Timer arrêté";
        String timeFormatted = formatTimeSpent(totalTimeSeconds);
        String message = String.format("Tâche \"%s\" terminée ! Temps total : %s", task.getTitle(), timeFormatted);
        return createNotification(task.getUser(), task, title, message, NotificationType.TIMER_STOPPED);
    }

    // ========================================
    // NOTIFICATIONS DE STATUT/PRIORITÉ
    // ========================================

    @Transactional
    public Notification notifyStatusChanged(Task task, String oldStatus, String newStatus) {
        String title = "📊 Statut modifié";
        String message = String.format("Tâche \"%s\" : %s → %s", task.getTitle(), formatStatus(oldStatus), formatStatus(newStatus));
        return createNotification(task.getUser(), task, title, message, NotificationType.STATUS_CHANGED);
    }

    @Transactional
    public Notification notifyPriorityChanged(Task task, String oldPriority, String newPriority) {
        String title = "🎯 Priorité modifiée";
        String message = String.format("Tâche \"%s\" : %s → %s", task.getTitle(), formatPriority(oldPriority), formatPriority(newPriority));
        return createNotification(task.getUser(), task, title, message, NotificationType.PRIORITY_CHANGED);
    }

    // ========================================
    // UTILITAIRES
    // ========================================

    private void cleanupOldNotifications(Long userId) {
        Long count = notificationRepository.countByUserId(userId);
        if (count > MAX_NOTIFICATIONS_PER_USER) {
            int toDelete = (int) (count - MAX_NOTIFICATIONS_PER_USER);
            List<Notification> oldest = notificationRepository.findOldestByUserId(userId);

            oldest.stream()
                    .sorted((a, b) -> {
                        if (a.getIsRead() && !b.getIsRead()) return -1;
                        if (!a.getIsRead() && b.getIsRead()) return 1;
                        return a.getCreatedAt().compareTo(b.getCreatedAt());
                    })
                    .limit(toDelete)
                    .forEach(n -> notificationRepository.deleteById(n.getId()));
        }
    }

    private String formatTimeSpent(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int mins = seconds / 60;
            int secs = seconds % 60;
            return String.format("%dmin %02ds", mins, secs);
        } else {
            int hours = seconds / 3600;
            int mins = (seconds % 3600) / 60;
            return String.format("%dh %02dmin", hours, mins);
        }
    }

    private String formatStatus(String status) {
        switch (status) {
            case "TODO": return "À faire";
            case "IN_PROGRESS": return "En cours";
            case "DONE": return "Terminé";
            default: return status;
        }
    }

    private String formatPriority(String priority) {
        switch (priority) {
            case "HIGH": return "🔴 Haute";
            case "MEDIUM": return "🟡 Moyenne";
            case "LOW": return "🟢 Basse";
            default: return priority;
        }
    }


    @Transactional
    public void deleteAllNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        notificationRepository.deleteByUserId(currentUser.getId());
    }
}