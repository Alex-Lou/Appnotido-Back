package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.Notification;
import com.example.AppNotiDo.dto.NotificationDTO;

public class NotificationMapper {

    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());

        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
        }

        if (notification.getTask() != null) {
            dto.setTaskId(notification.getTask().getId());
            dto.setTaskTitle(notification.getTask().getTitle());
        }

        return dto;
    }
}