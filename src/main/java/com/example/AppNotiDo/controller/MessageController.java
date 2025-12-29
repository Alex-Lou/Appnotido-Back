package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.dto.MessageDTO;
import com.example.AppNotiDo.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * GET /api/messages - Tous mes messages
     */
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    /**
     * GET /api/messages/unread - Messages non lus
     */
    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages() {
        return ResponseEntity.ok(messageService.getUnreadMessages());
    }

    /**
     * GET /api/messages/count - Nombre de messages non lus
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countUnread() {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread()));
    }

    /**
     * GET /api/messages/project/{projectId} - Messages d'un projet
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(messageService.getMessagesByProject(projectId));
    }

    /**
     * PUT /api/messages/{id}/read - Marquer comme lu
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    /**
     * PUT /api/messages/read-all - Marquer tous comme lus
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        messageService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/messages/{id} - Supprimer un message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/messages - Supprimer tous mes messages
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllMessages() {
        messageService.deleteAllMessages();
        return ResponseEntity.noContent().build();
    }
}