package com.example.AppNotiDo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime dueDate;
    private Integer estimatedDuration;
    private Integer reminderMinutes;
    private Boolean notified;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean locked;
    private String tags;

    private LocalDateTime startedAt;
    private LocalDateTime pausedAt;
    private Integer timeSpent;
    private Boolean isRunning;
    private Boolean reactivable;
    private Boolean timerEnabled;

    private Long projectId;
    private String projectName;
    private String projectColor;

    // ===== SOUS-TÂCHES =====
    private List<SubtaskDTO> subtasks;
    private Integer subtaskCount;
    private Integer completedSubtaskCount;
    private Double subtaskProgress;

    // ===== RÉCURRENCE =====
    private String recurrenceType;          // NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    private Integer recurrenceInterval;      // Tous les X jours/semaines/mois
    private String recurrenceDays;           // Pour hebdo: "MONDAY,WEDNESDAY,FRIDAY"
    private Integer recurrenceDayOfMonth;    // Pour mensuel: 1-31
    private LocalDateTime recurrenceEndDate; // Date de fin optionnelle
    private LocalDateTime nextOccurrence;    // Prochaine date de création
    private Long parentTaskId;               // ID de la tâche template si générée
    private Boolean isRecurringTemplate;     // True si c'est le modèle de récurrence
}