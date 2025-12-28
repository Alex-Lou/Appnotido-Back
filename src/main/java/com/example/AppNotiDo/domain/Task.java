package com.example.AppNotiDo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title can not be empty")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "reminder_minutes")
    private Integer reminderMinutes = 15;

    @Column(name = "notified")
    private Boolean notified = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // ===== RELATION PROJET =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    // ===== RELATION SOUS-TÂCHES =====
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Subtask> subtasks = new ArrayList<>();

    @Column(name = "locked")
    private Boolean locked = false;

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "time_spent")
    private Integer timeSpent = 0;

    @Column(name = "is_running")
    private Boolean isRunning = false;

    @Column(name = "reactivable", nullable = false)
    private Boolean reactivable = false;

    @Column(name = "timer_enabled", nullable = false)
    private Boolean timerEnabled = true;

    // ===== RÉCURRENCE =====
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type")
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Column(name = "recurrence_interval")
    private Integer recurrenceInterval = 1; // Tous les X jours/semaines/mois

    @Column(name = "recurrence_days", length = 100)
    private String recurrenceDays; // Pour hebdo: "MONDAY,WEDNESDAY,FRIDAY"

    @Column(name = "recurrence_day_of_month")
    private Integer recurrenceDayOfMonth; // Pour mensuel: 1-31

    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate; // Date de fin optionnelle

    @Column(name = "next_occurrence")
    private LocalDateTime nextOccurrence; // Prochaine date de création

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    @JsonIgnore
    private Task parentTask; // Tâche template si générée par récurrence

    @Column(name = "is_recurring_template")
    private Boolean isRecurringTemplate = false; // True si c'est le modèle de récurrence

    // ===== MÉTHODES UTILITAIRES SOUS-TÂCHES =====
    public int getSubtaskCount() {
        return subtasks != null ? subtasks.size() : 0;
    }

    public int getCompletedSubtaskCount() {
        if (subtasks == null) return 0;
        return (int) subtasks.stream()
                .filter(s -> Boolean.TRUE.equals(s.getCompleted()))
                .count();
    }

    public double getSubtaskProgress() {
        int total = getSubtaskCount();
        if (total == 0) return 0.0;
        return (getCompletedSubtaskCount() * 100.0) / total;
    }

    // ===== MÉTHODES UTILITAIRES RÉCURRENCE =====
    public boolean isRecurring() {
        return recurrenceType != null && recurrenceType != RecurrenceType.NONE;
    }
}