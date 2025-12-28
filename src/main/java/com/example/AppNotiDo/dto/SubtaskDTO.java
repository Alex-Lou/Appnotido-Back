package com.example.AppNotiDo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubtaskDTO {

    private Long id;
    private String title;
    private Boolean completed;
    private Integer displayOrder;
    private Long taskId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}