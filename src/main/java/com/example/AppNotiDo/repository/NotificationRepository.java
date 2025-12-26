package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.Notification;
import com.example.AppNotiDo.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupérer toutes les notifications d'un user, triées par date (plus récentes en premier)
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Récupérer les notifications non lues d'un user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Compter les notifications non lues
    Long countByUserIdAndIsReadFalse(Long userId);

    // Compter toutes les notifications d'un user
    Long countByUserId(Long userId);

    // Récupérer les X notifications les plus anciennes d'un user
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt ASC")
    List<Notification> findOldestByUserId(@Param("userId") Long userId);

    // Supprimer les notifications les plus anciennes au-delà de la limite
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN " +
            "(SELECT n2.id FROM Notification n2 WHERE n2.user.id = :userId ORDER BY n2.createdAt ASC LIMIT :count)")
    void deleteOldestByUserId(@Param("userId") Long userId, @Param("count") int count);

    // Vérifier si une notification existe déjà pour une tâche et un type donné (éviter les doublons)
    boolean existsByTaskIdAndTypeAndCreatedAtAfter(Long taskId, NotificationType type, LocalDateTime after);

    // Supprimer les notifications d'une tâche (quand la tâche est supprimée)
    void deleteByTaskId(Long taskId);

    // Marquer toutes les notifications d'un user comme lues
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    void deleteByUserId(Long userId);

}