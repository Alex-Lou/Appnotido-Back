package com.example.AppNotiDo.dto;

import com.example.AppNotiDo.domain.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long recipientId;
    private String recipientUsername;
    private Long senderId;
    private String senderUsername;
    private String senderDisplayName;
    private Long projectId;
    private String projectName;
    private MessageType type;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}