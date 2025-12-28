package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.*;
import com.example.AppNotiDo.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecurrenceService {

    private static final Logger logger = LoggerFactory.getLogger(RecurrenceService.class);

    private final TaskRepository taskRepository;

    public RecurrenceService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Calcule la prochaine date d'occurrence basée sur le type de récurrence
     */
    public LocalDateTime calculateNextOccurrence(Task task) {
        if (task.getRecurrenceType() == null || task.getRecurrenceType() == RecurrenceType.NONE) {
            return null;
        }

        LocalDateTime baseDate = task.getDueDate() != null ? task.getDueDate() : LocalDateTime.now();
        int interval = task.getRecurrenceInterval() != null ? task.getRecurrenceInterval() : 1;

        switch (task.getRecurrenceType()) {
            case DAILY:
                return calculateNextDaily(baseDate, interval);
            case WEEKLY:
                return calculateNextWeekly(baseDate, interval, task.getRecurrenceDays());
            case MONTHLY:
                return calculateNextMonthly(baseDate, interval, task.getRecurrenceDayOfMonth());
            case YEARLY:
                return calculateNextYearly(baseDate, interval);
            default:
                return null;
        }
    }

    private LocalDateTime calculateNextDaily(LocalDateTime baseDate, int interval) {
        LocalDateTime next = baseDate.plusDays(interval);
        // S'assurer que la date est dans le futur
        while (next.isBefore(LocalDateTime.now())) {
            next = next.plusDays(interval);
        }
        return next;
    }

    private LocalDateTime calculateNextWeekly(LocalDateTime baseDate, int interval, String recurrenceDays) {
        if (recurrenceDays == null || recurrenceDays.isEmpty()) {
            // Par défaut, même jour de la semaine
            LocalDateTime next = baseDate.plusWeeks(interval);
            while (next.isBefore(LocalDateTime.now())) {
                next = next.plusWeeks(interval);
            }
            return next;
        }

        // Parser les jours de la semaine
        Set<DayOfWeek> days = Arrays.stream(recurrenceDays.split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        LocalDateTime next = baseDate;
        int maxIterations = 100; // Sécurité pour éviter boucle infinie
        int iterations = 0;

        while (iterations < maxIterations) {
            next = next.plusDays(1);
            if (days.contains(next.getDayOfWeek()) && next.isAfter(LocalDateTime.now())) {
                return next;
            }
            iterations++;
        }

        // Fallback: semaine suivante
        return baseDate.plusWeeks(interval);
    }

    private LocalDateTime calculateNextMonthly(LocalDateTime baseDate, int interval, Integer dayOfMonth) {
        int day = dayOfMonth != null ? dayOfMonth : baseDate.getDayOfMonth();

        LocalDateTime next = baseDate.plusMonths(interval);

        // Gérer les mois avec moins de jours (ex: 31 février n'existe pas)
        int maxDay = next.toLocalDate().lengthOfMonth();
        int actualDay = Math.min(day, maxDay);

        next = next.withDayOfMonth(actualDay);

        while (next.isBefore(LocalDateTime.now())) {
            next = next.plusMonths(interval);
            maxDay = next.toLocalDate().lengthOfMonth();
            actualDay = Math.min(day, maxDay);
            next = next.withDayOfMonth(actualDay);
        }

        return next;
    }

    private LocalDateTime calculateNextYearly(LocalDateTime baseDate, int interval) {
        LocalDateTime next = baseDate.plusYears(interval);
        while (next.isBefore(LocalDateTime.now())) {
            next = next.plusYears(interval);
        }
        return next;
    }

    /**
     * Crée une nouvelle occurrence d'une tâche récurrente
     */
    @Transactional
    public Task createNextOccurrence(Task templateTask) {
        if (!templateTask.isRecurring()) {
            return null;
        }

        // Vérifier si la récurrence est terminée
        if (templateTask.getRecurrenceEndDate() != null
                && LocalDateTime.now().isAfter(templateTask.getRecurrenceEndDate())) {
            logger.info("Récurrence terminée pour la tâche: {}", templateTask.getTitle());
            return null;
        }

        // Calculer la prochaine date
        LocalDateTime nextDueDate = calculateNextOccurrence(templateTask);
        if (nextDueDate == null) {
            return null;
        }

        // Créer la nouvelle tâche
        Task newTask = new Task();
        newTask.setTitle(templateTask.getTitle());
        newTask.setDescription(templateTask.getDescription());
        newTask.setStatus(TaskStatus.TODO);
        newTask.setPriority(templateTask.getPriority());
        newTask.setDueDate(nextDueDate);
        newTask.setEstimatedDuration(templateTask.getEstimatedDuration());
        newTask.setReminderMinutes(templateTask.getReminderMinutes());
        newTask.setUser(templateTask.getUser());
        newTask.setProject(templateTask.getProject());
        newTask.setTags(templateTask.getTags());
        newTask.setTimerEnabled(templateTask.getTimerEnabled());
        newTask.setReactivable(templateTask.getReactivable());

        // Lier à la tâche parent
        newTask.setParentTask(templateTask);

        // La nouvelle tâche n'est PAS un template
        newTask.setIsRecurringTemplate(false);
        newTask.setRecurrenceType(RecurrenceType.NONE);

        // Mettre à jour la prochaine occurrence sur le template
        templateTask.setNextOccurrence(calculateNextOccurrence(newTask));
        taskRepository.save(templateTask);

        return taskRepository.save(newTask);
    }

    /**
     * Job planifié qui s'exécute toutes les heures pour créer les tâches récurrentes
     */
    @Scheduled(fixedRate = 3600000) // Toutes les heures
    @Transactional
    public void processRecurringTasks() {
        logger.info("Traitement des tâches récurrentes...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lookAhead = now.plusHours(24); // Regarder 24h en avance

        // Trouver les templates de récurrence dont la prochaine occurrence est proche
        List<Task> recurringTemplates = taskRepository.findByIsRecurringTemplateTrueAndNextOccurrenceBefore(lookAhead);

        for (Task template : recurringTemplates) {
            try {
                if (template.getNextOccurrence() != null && template.getNextOccurrence().isBefore(lookAhead)) {
                    Task newTask = createNextOccurrence(template);
                    if (newTask != null) {
                        logger.info("Créé occurrence récurrente: {} pour {}", newTask.getTitle(), newTask.getDueDate());
                    }
                }
            } catch (Exception e) {
                logger.error("Erreur lors de la création de l'occurrence pour la tâche {}: {}",
                        template.getId(), e.getMessage());
            }
        }

        logger.info("Traitement des tâches récurrentes terminé.");
    }

    /**
     * Configure une tâche comme template de récurrence
     */
    @Transactional
    public Task setupRecurrence(Task task, RecurrenceType type, Integer interval,
                                String days, Integer dayOfMonth, LocalDateTime endDate) {
        task.setRecurrenceType(type);
        task.setRecurrenceInterval(interval != null ? interval : 1);
        task.setRecurrenceDays(days);
        task.setRecurrenceDayOfMonth(dayOfMonth);
        task.setRecurrenceEndDate(endDate);
        task.setIsRecurringTemplate(type != RecurrenceType.NONE);

        // Calculer la première prochaine occurrence
        if (type != RecurrenceType.NONE) {
            task.setNextOccurrence(calculateNextOccurrence(task));
        } else {
            task.setNextOccurrence(null);
        }

        return taskRepository.save(task);
    }

    /**
     * Arrête la récurrence d'une tâche
     */
    @Transactional
    public Task stopRecurrence(Task task) {
        task.setRecurrenceType(RecurrenceType.NONE);
        task.setIsRecurringTemplate(false);
        task.setNextOccurrence(null);
        return taskRepository.save(task);
    }
}