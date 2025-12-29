package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.Message;
import com.example.AppNotiDo.dto.MessageDTO;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }

        return MessageDTO.builder()
                .id(message.getId())
                .recipientId(message.getRecipient().getId())
                .recipientUsername(message.getRecipient().getUsername())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderUsername(message.getSender() != null ? message.getSender().getUsername() : null)
                .senderDisplayName(message.getSender() != null ?
                        (message.getSender().getDisplayName() != null ?
                                message.getSender().getDisplayName() :
                                message.getSender().getUsername()) :
                        "Système")
                .projectId(message.getProject() != null ? message.getProject().getId() : null)
                .projectName(message.getProject() != null ? message.getProject().getName() : null)
                .type(message.getType())
                .title(message.getTitle())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}