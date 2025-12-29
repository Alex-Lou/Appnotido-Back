package com.example.AppNotiDo.repository;

import com.example.AppNotiDo.domain.MemberStatus;
import com.example.AppNotiDo.domain.ProjectMember;
import com.example.AppNotiDo.domain.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // Trouver tous les membres d'un projet
    List<ProjectMember> findByProjectId(Long projectId);

    // Membres actifs d'un projet uniquement
    List<ProjectMember> findByProjectIdAndStatus(Long projectId, MemberStatus status);

    // Vérifier si un user est membre d'un projet
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    // Vérifier existence (pour éviter doublons)
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    // Tous les projets où un user est membre (actif)
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId AND pm.status = :status")
    List<ProjectMember> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") MemberStatus status);

    // Trouver le owner d'un projet
    Optional<ProjectMember> findByProjectIdAndRole(Long projectId, ProjectRole role);

    // Invitations en attente pour un user
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId AND pm.status = 'PENDING'")
    List<ProjectMember> findPendingInvitationsForUser(@Param("userId") Long userId);

    // Compter les membres actifs d'un projet
    long countByProjectIdAndStatus(Long projectId, MemberStatus status);

    // Supprimer tous les membres d'un projet (utile si on supprime le projet)
    void deleteByProjectId(Long projectId);

    List<ProjectMember> findByProjectIdAndStatusIn(Long projectId, List<MemberStatus> statuses);


}