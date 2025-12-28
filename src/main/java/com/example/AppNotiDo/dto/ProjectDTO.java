package com.example.AppNotiDo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Boolean isArchived;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Stats calculées
    private Integer taskCount;
    private Integer completedTaskCount;
    private Integer pendingTaskCount;
    private Double completionPercentage;

    public Double getCompletionPercentage() {
        if (taskCount == null || taskCount == 0) return 0.0;
        return (completedTaskCount != null ? completedTaskCount : 0) * 100.0 / taskCount;
    }
}