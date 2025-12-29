package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.Message;
import com.example.AppNotiDo.domain.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Tous les messages d'un user (triés par date décroissante)
    List<Message> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Messages non lus d'un user
    List<Message> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);

    // Compter les messages non lus
    Long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Messages par type
    List<Message> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long recipientId, MessageType type);

    // Messages liés à un projet
    List<Message> findByRecipientIdAndProjectIdOrderByCreatedAtDesc(Long recipientId, Long projectId);

    // Marquer tous comme lus
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt WHERE m.recipient.id = :userId AND m.isRead = false")
    void markAllAsReadByRecipientId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    // Supprimer les messages d'un user
    void deleteByRecipientId(Long recipientId);

    // Supprimer les messages liés à un projet
    void deleteByProjectId(Long projectId);

    // Compter total messages d'un user
    Long countByRecipientId(Long recipientId);

    // Messages les plus anciens (pour cleanup)
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId ORDER BY m.createdAt ASC")
    List<Message> findOldestByRecipientId(@Param("userId") Long userId);
}