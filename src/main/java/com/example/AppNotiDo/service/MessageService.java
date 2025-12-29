package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.*;
import com.example.AppNotiDo.dto.MessageDTO;
import com.example.AppNotiDo.mapper.MessageMapper;
import com.example.AppNotiDo.repository.MessageRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private static final int MAX_MESSAGES_PER_USER = 100;

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final SecurityUtils securityUtils;

    // ========================================
    // LECTURE DES MESSAGES
    // ========================================

    public List<MessageDTO> getAllMessages() {
        User currentUser = securityUtils.getCurrentUser();
        return messageRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getUnreadMessages() {
        User currentUser = securityUtils.getCurrentUser();
        return messageRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Long countUnread() {
        User currentUser = securityUtils.getCurrentUser();
        return messageRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());
    }

    public List<MessageDTO> getMessagesByProject(Long projectId) {
        User currentUser = securityUtils.getCurrentUser();
        return messageRepository.findByRecipientIdAndProjectIdOrderByCreatedAtDesc(currentUser.getId(), projectId)
                .stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // ACTIONS SUR LES MESSAGES
    // ========================================

    public MessageDTO markAsRead(Long messageId) {
        User currentUser = securityUtils.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (!message.getRecipient().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        message.markAsRead();
        return messageMapper.toDTO(messageRepository.save(message));
    }

    public void markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        messageRepository.markAllAsReadByRecipientId(currentUser.getId(), LocalDateTime.now());
    }

    public void deleteMessage(Long messageId) {
        User currentUser = securityUtils.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message non trouvé"));

        if (!message.getRecipient().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Accès refusé");
        }

        messageRepository.delete(message);
    }

    public void deleteAllMessages() {
        User currentUser = securityUtils.getCurrentUser();
        messageRepository.deleteByRecipientId(currentUser.getId());
    }

    // ========================================
    // CRÉATION DE MESSAGES
    // ========================================

    public Message createMessage(User recipient, User sender, Project project,
                                 MessageType type, String title, String content) {
        Message message = Message.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(type)
                .title(title)
                .content(content)
                .build();

        message = messageRepository.save(message);
        cleanupOldMessages(recipient.getId());
        return message;
    }

    // ========================================
    // MESSAGES PRÉDÉFINIS - INVITATIONS
    // ========================================

    public void sendInvitationReceived(User recipient, User sender, Project project) {
        createMessage(
                recipient,
                sender,
                project,
                MessageType.INVITATION_RECEIVED,
                "📨 Invitation reçue",
                String.format("%s vous a invité à rejoindre le projet \"%s\"",
                        sender.getUsername(), project.getName())
        );
    }

    public void sendInvitationAccepted(User recipient, User acceptedBy, Project project) {
        createMessage(
                recipient,
                acceptedBy,
                project,
                MessageType.INVITATION_ACCEPTED,
                "✅ Invitation acceptée",
                String.format("%s a accepté votre invitation à rejoindre le projet \"%s\"",
                        acceptedBy.getUsername(), project.getName())
        );
    }

    public void sendInvitationDeclined(User recipient, User declinedBy, Project project) {
        createMessage(
                recipient,
                declinedBy,
                project,
                MessageType.INVITATION_DECLINED,
                "❌ Invitation déclinée",
                String.format("%s a décliné votre invitation pour le projet \"%s\"",
                        declinedBy.getUsername(), project.getName())
        );
    }

    // ========================================
    // MESSAGES PRÉDÉFINIS - MEMBRES
    // ========================================

    public void sendMemberJoined(User recipient, User newMember, Project project) {
        createMessage(
                recipient,
                newMember,
                project,
                MessageType.MEMBER_JOINED,
                "👋 Nouveau membre",
                String.format("%s a rejoint votre projet \"%s\"",
                        newMember.getUsername(), project.getName())
        );
    }

    public void sendMemberRemoved(User recipient, User removedBy, Project project) {
        createMessage(
                recipient,
                removedBy,
                project,
                MessageType.MEMBER_REMOVED,
                "🚫 Retrait du projet",
                String.format("Vous avez été retiré du projet \"%s\"", project.getName())
        );
    }

    public void sendMemberRoleChanged(User recipient, User changedBy, Project project,
                                      String oldRole, String newRole) {
        createMessage(
                recipient,
                changedBy,
                project,
                MessageType.MEMBER_ROLE_CHANGED,
                "🔄 Rôle modifié",
                String.format("Votre rôle dans le projet \"%s\" a été changé de %s à %s",
                        project.getName(), formatRole(oldRole), formatRole(newRole))
        );
    }

    // ========================================
    // MESSAGES PRÉDÉFINIS - PROPRIÉTÉ
    // ========================================

    public void sendOwnershipReceived(User recipient, User previousOwner, Project project) {
        createMessage(
                recipient,
                previousOwner,
                project,
                MessageType.OWNERSHIP_RECEIVED,
                "👑 Transfert de propriété",
                String.format("Vous êtes maintenant propriétaire du projet \"%s\"", project.getName())
        );
    }

    public void sendOwnershipTransferred(User recipient, User newOwner, Project project) {
        createMessage(
                recipient,
                newOwner,
                project,
                MessageType.OWNERSHIP_TRANSFERRED,
                "📋 Propriété transférée",
                String.format("Vous avez transféré la propriété du projet \"%s\" à %s. Vous êtes maintenant administrateur.",
                        project.getName(), newOwner.getUsername())
        );
    }

    // ========================================
    // UTILITAIRES
    // ========================================

    private void cleanupOldMessages(Long userId) {
        Long count = messageRepository.countByRecipientId(userId);
        if (count > MAX_MESSAGES_PER_USER) {
            int toDelete = (int) (count - MAX_MESSAGES_PER_USER);
            List<Message> oldest = messageRepository.findOldestByRecipientId(userId);

            oldest.stream()
                    .sorted((a, b) -> {
                        // Supprimer d'abord les messages lus
                        if (a.getIsRead() && !b.getIsRead()) return -1;
                        if (!a.getIsRead() && b.getIsRead()) return 1;
                        return a.getCreatedAt().compareTo(b.getCreatedAt());
                    })
                    .limit(toDelete)
                    .forEach(m -> messageRepository.deleteById(m.getId()));
        }
    }

    private String formatRole(String role) {
        switch (role) {
            case "OWNER": return "Propriétaire";
            case "ADMIN": return "Administrateur";
            case "MEMBER": return "Membre";
            case "VIEWER": return "Lecteur";
            default: return role;
        }
    }
}