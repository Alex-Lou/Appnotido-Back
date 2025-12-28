package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    // Trouver toutes les sous-tâches d'une tâche
    List<Subtask> findByTaskIdOrderByDisplayOrderAsc(Long taskId);

    // Trouver une sous-tâche par ID et taskId (sécurité)
    Optional<Subtask> findByIdAndTaskId(Long id, Long taskId);

    // Compter les sous-tâches d'une tâche
    long countByTaskId(Long taskId);

    // Compter les sous-tâches complétées d'une tâche
    long countByTaskIdAndCompletedTrue(Long taskId);

    // Trouver le displayOrder maximum pour une tâche
    @Query("SELECT COALESCE(MAX(s.displayOrder), 0) FROM Subtask s WHERE s.task.id = :taskId")
    Integer findMaxDisplayOrderByTaskId(@Param("taskId") Long taskId);

    // Supprimer toutes les sous-tâches d'une tâche
    @Modifying
    void deleteByTaskId(Long taskId);

    // Vérifier si une sous-tâche appartient à une tâche d'un utilisateur
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subtask s WHERE s.id = :subtaskId AND s.task.user.id = :userId")
    boolean existsByIdAndTaskUserId(@Param("subtaskId") Long subtaskId, @Param("userId") Long userId);

    // Trouver une sous-tâche avec vérification utilisateur
    @Query("SELECT s FROM Subtask s WHERE s.id = :subtaskId AND s.task.user.id = :userId")
    Optional<Subtask> findByIdAndTaskUserId(@Param("subtaskId") Long subtaskId, @Param("userId") Long userId);
}