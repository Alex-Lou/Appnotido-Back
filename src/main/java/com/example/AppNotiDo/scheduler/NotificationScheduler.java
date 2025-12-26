package com.example.AppNotiDo.scheduler;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(TaskRepository taskRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    /**
     * Vérifie toutes les minutes les tâches qui nécessitent une notification
     */
    @Scheduled(fixedRate = 60000) // Toutes les 60 secondes
    @Transactional
    public void checkTasksForNotifications() {
        LocalDateTime now = LocalDateTime.now();

        logger.debug("Checking tasks for notifications at {}", now);

        // Récupérer toutes les tâches non terminées avec une date d'échéance
        List<Task> tasksWithDueDate = taskRepository.findByStatusNotAndDueDateIsNotNull(TaskStatus.DONE);

        for (Task task : tasksWithDueDate) {
            if (task.getDueDate() == null || task.getUser() == null) {
                continue;
            }

            LocalDateTime dueDate = task.getDueDate();
            Integer reminderMinutes = task.getReminderMinutes() != null ? task.getReminderMinutes() : 15;
            LocalDateTime reminderTime = dueDate.minusMinutes(reminderMinutes);

            // Notification de rappel (X minutes avant l'échéance)
            // Seulement si pas encore notifié ET on est dans la fenêtre de rappel
            if (!Boolean.TRUE.equals(task.getNotified()) &&
                    now.isAfter(reminderTime) &&
                    now.isBefore(dueDate)) {

                logger.info("Creating reminder notification for task: {}", task.getTitle());
                notificationService.createReminderNotification(task);

                // Marquer la tâche comme notifiée pour éviter les doublons
                task.setNotified(true);
                taskRepository.save(task);
            }

            // Notification d'échéance atteinte (dans la minute de l'échéance)
            if (now.isAfter(dueDate) && now.isBefore(dueDate.plusMinutes(1))) {
                logger.info("Creating deadline notification for task: {}", task.getTitle());
                notificationService.createDeadlineNotification(task);
            }

            // Notification de retard (tâche non terminée, échéance dépassée de plus de 5 minutes)
            // Le service NotificationService gère déjà les doublons avec un check sur les 5 dernières minutes
            if (now.isAfter(dueDate.plusMinutes(5)) && task.getStatus() != TaskStatus.DONE) {
                logger.info("Creating overdue notification for task: {}", task.getTitle());
                notificationService.createOverdueNotification(task);
            }
        }
    }
}