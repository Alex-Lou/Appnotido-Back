package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.dto.TaskDTO;

public class TaskMapper {

    public static TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }

        TaskDTO dto = new TaskDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setEstimatedDuration(task.getEstimatedDuration());
        dto.setReminderMinutes(task.getReminderMinutes());
        dto.setNotified(task.getNotified());
        dto.setLocked(task.getLocked() != null ? task.getLocked() : false);
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        dto.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        dto.setPriority(task.getPriority() != null ? task.getPriority().name() : null);

        // User infos
        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
            dto.setUsername(task.getUser().getUsername());
        }

        // Tags
        dto.setTags(task.getTags());

        // Timer fields
        dto.setStartedAt(task.getStartedAt());
        dto.setPausedAt(task.getPausedAt());
        dto.setTimeSpent(task.getTimeSpent() != null ? task.getTimeSpent() : 0);
        dto.setIsRunning(task.getIsRunning() != null ? task.getIsRunning() : false);

        // Reactivable field
        dto.setReactivable(task.getReactivable() != null ? task.getReactivable() : false);

        // Timer enabled field
        dto.setTimerEnabled(task.getTimerEnabled() != null ? task.getTimerEnabled() : true);

        return dto;
    }

    public static Task toEntity(TaskDTO dto) {
        if (dto == null) {
            return null;
        }

        Task task = new Task();

        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(dto.getStatus()));
        }

        if (dto.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(dto.getPriority()));
        }

        task.setDueDate(dto.getDueDate());
        task.setEstimatedDuration(dto.getEstimatedDuration());
        task.setReminderMinutes(dto.getReminderMinutes());
        task.setNotified(dto.getNotified());
        task.setLocked(dto.isLocked());
        task.setCreatedAt(dto.getCreatedAt());
        task.setUpdatedAt(dto.getUpdatedAt());

        // Tags
        task.setTags(dto.getTags());

        // Timer fields
        task.setStartedAt(dto.getStartedAt());
        task.setPausedAt(dto.getPausedAt());
        task.setTimeSpent(dto.getTimeSpent());
        task.setIsRunning(dto.getIsRunning());

        // Reactivable field
        task.setReactivable(dto.getReactivable() != null ? dto.getReactivable() : false);

        // Timer enabled field
        task.setTimerEnabled(dto.getTimerEnabled() != null ? dto.getTimerEnabled() : true);

        return task;
    }
}
