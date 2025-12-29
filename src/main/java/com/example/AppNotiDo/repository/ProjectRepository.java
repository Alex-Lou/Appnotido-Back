package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Trouver tous les projets d'un utilisateur (non archivés) AVEC les tâches
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId AND p.isArchived = false ORDER BY p.displayOrder ASC")
    List<Project> findByUserIdAndIsArchivedFalseOrderByDisplayOrderAsc(@Param("userId") Long userId);

    // Trouver tous les projets d'un utilisateur (incluant archivés) AVEC les tâches
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId ORDER BY p.displayOrder ASC")
    List<Project> findByUserIdOrderByDisplayOrderAsc(@Param("userId") Long userId);

    // Trouver les projets archivés d'un utilisateur AVEC les tâches
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId AND p.isArchived = true ORDER BY p.updatedAt DESC")
    List<Project> findByUserIdAndIsArchivedTrueOrderByUpdatedAtDesc(@Param("userId") Long userId);

    // Trouver un projet par ID et userId (sécurité) AVEC les tâches
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id AND p.user.id = :userId")
    Optional<Project> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // Vérifier si un projet appartient à un utilisateur
    boolean existsByIdAndUserId(Long id, Long userId);

    // Compter les projets d'un utilisateur
    long countByUserIdAndIsArchivedFalse(Long userId);

    // Trouver le displayOrder maximum pour un utilisateur
    @Query("SELECT COALESCE(MAX(p.displayOrder), 0) FROM Project p WHERE p.user.id = :userId")
    Integer findMaxDisplayOrderByUserId(@Param("userId") Long userId);

    // Rechercher des projets par nom AVEC les tâches
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isArchived = false")
    List<Project> findByUserIdAndNameContainingIgnoreCaseAndIsArchivedFalse(@Param("userId") Long userId, @Param("name") String name);

    Page<Project> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByCreatedAtAfter(LocalDateTime date);

}