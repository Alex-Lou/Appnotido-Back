package com.example.AppNotiDo.dto;
import lombok.Data;
import java.time.LocalDateTime;

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
}