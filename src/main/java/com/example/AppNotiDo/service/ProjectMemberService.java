package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.*;
import com.example.AppNotiDo.dto.ProjectMemberDTO;
import com.example.AppNotiDo.mapper.ProjectMemberMapper;
import com.example.AppNotiDo.repository.ProjectMemberRepository;
import com.example.AppNotiDo.repository.ProjectRepository;
import com.example.AppNotiDo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final MessageService messageService;

    // ========================================
    // LECTURE DES MEMBRES
    // ========================================

    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectIdAndStatusIn(projectId,
                        List.of(MemberStatus.ACTIVE, MemberStatus.PENDING)).stream()
                .map(projectMemberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProjectMemberDTO> getActiveProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectIdAndStatus(projectId, MemberStatus.ACTIVE).stream()
                .map(projectMemberMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========================================
    // INVITATION
    // ========================================

    public ProjectMemberDTO inviteMember(Long projectId, String usernameOrEmail, ProjectRole role, Long invitedById) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        User userToInvite = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + usernameOrEmail)));

        // ✅ MODIFIÉ : On peut maintenant inviter comme OWNER
        User invitedBy = userRepository.findById(invitedById)
                .orElseThrow(() -> new RuntimeException("Utilisateur invitant non trouvé"));

        // Vérifier que seul un OWNER peut inviter comme OWNER
        if (role == ProjectRole.OWNER) {
            ProjectMember inviter = projectMemberRepository.findByProjectIdAndUserId(projectId, invitedById)
                    .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce projet"));
            if (!inviter.isOwner()) {
                throw new RuntimeException("Seul un propriétaire peut promouvoir quelqu'un comme propriétaire");
            }
        }

        Optional<ProjectMember> existingMember = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userToInvite.getId());

        ProjectMember member;

        if (existingMember.isPresent()) {
            ProjectMember existing = existingMember.get();

            if (existing.getStatus() == MemberStatus.ACTIVE || existing.getStatus() == MemberStatus.PENDING) {
                throw new RuntimeException("Cet utilisateur est déjà membre du projet");
            }

            existing.setRole(role);
            existing.setStatus(MemberStatus.PENDING);
            existing.setInvitedBy(invitedBy);
            existing.setJoinedAt(LocalDateTime.now());
            existing.setAcceptedAt(null);
            member = projectMemberRepository.save(existing);
        } else {
            member = ProjectMember.builder()
                    .project(project)
                    .user(userToInvite)
                    .role(role)
                    .status(MemberStatus.PENDING)
                    .invitedBy(invitedBy)
                    .build();
            member = projectMemberRepository.save(member);
        }

        messageService.sendInvitationReceived(userToInvite, invitedBy, project);

        return projectMemberMapper.toDTO(member);
    }

    // ========================================
    // ACCEPTER / REFUSER INVITATION
    // ========================================

    public ProjectMemberDTO acceptInvitation(Long memberId, Long userId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Invitation non trouvée"));

        if (!member.getUser().getId().equals(userId)) {
            throw new RuntimeException("Cette invitation ne vous appartient pas");
        }

        if (member.getStatus() != MemberStatus.PENDING) {
            throw new RuntimeException("Cette invitation n'est plus en attente");
        }

        member.accept();
        ProjectMember saved = projectMemberRepository.save(member);

        Project project = member.getProject();
        User acceptingUser = member.getUser();

        if (member.getInvitedBy() != null) {
            messageService.sendInvitationAccepted(member.getInvitedBy(), acceptingUser, project);
        }

        // Notifier tous les OWNERS (il peut y en avoir plusieurs)
        List<ProjectMember> owners = projectMemberRepository.findByProjectIdAndStatus(project.getId(), MemberStatus.ACTIVE)
                .stream()
                .filter(ProjectMember::isOwner)
                .filter(owner -> !owner.getUser().getId().equals(member.getInvitedBy() != null ? member.getInvitedBy().getId() : null))
                .collect(Collectors.toList());

        for (ProjectMember owner : owners) {
            messageService.sendMemberJoined(owner.getUser(), acceptingUser, project);
        }

        return projectMemberMapper.toDTO(saved);
    }

    public void declineInvitation(Long memberId, Long userId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Invitation non trouvée"));

        if (!member.getUser().getId().equals(userId)) {
            throw new RuntimeException("Cette invitation ne vous appartient pas");
        }

        member.setStatus(MemberStatus.DECLINED);
        projectMemberRepository.save(member);

        if (member.getInvitedBy() != null) {
            messageService.sendInvitationDeclined(member.getInvitedBy(), member.getUser(), member.getProject());
        }
    }

    // ========================================
    // GESTION DES MEMBRES
    // ========================================

    public void removeMember(Long projectId, Long memberUserId, Long requesterId) {
        ProjectMember requester = projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce projet"));

        if (!requester.canManageMembers()) {
            throw new RuntimeException("Vous n'avez pas les droits pour retirer des membres");
        }

        ProjectMember memberToRemove = projectMemberRepository.findByProjectIdAndUserId(projectId, memberUserId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        // ✅ MODIFIÉ : On peut retirer un OWNER seulement s'il y en a d'autres
        if (memberToRemove.isOwner()) {
            long ownerCount = projectMemberRepository.findByProjectIdAndStatus(projectId, MemberStatus.ACTIVE)
                    .stream()
                    .filter(ProjectMember::isOwner)
                    .count();

            if (ownerCount <= 1) {
                throw new RuntimeException("Impossible de retirer le dernier propriétaire du projet");
            }

            // Seul un autre OWNER peut retirer un OWNER
            if (!requester.isOwner()) {
                throw new RuntimeException("Seul un propriétaire peut retirer un autre propriétaire");
            }
        }

        if (memberToRemove.getRole() == ProjectRole.ADMIN && !requester.isOwner()) {
            throw new RuntimeException("Seul un propriétaire peut retirer un administrateur");
        }

        Project project = memberToRemove.getProject();
        User removedUser = memberToRemove.getUser();

        if (memberToRemove.getStatus() == MemberStatus.PENDING) {
            projectMemberRepository.delete(memberToRemove);
        } else {
            memberToRemove.setStatus(MemberStatus.REMOVED);
            projectMemberRepository.save(memberToRemove);
        }

        messageService.sendMemberRemoved(removedUser, requester.getUser(), project);
    }

    public ProjectMemberDTO updateMemberRole(Long projectId, Long memberUserId, ProjectRole newRole, Long requesterId) {
        ProjectMember requester = projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce projet"));

        if (!requester.isOwner()) {
            throw new RuntimeException("Seul un propriétaire peut modifier les rôles");
        }

        ProjectMember memberToUpdate = projectMemberRepository.findByProjectIdAndUserId(projectId, memberUserId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        // ✅ MODIFIÉ : Un OWNER peut être rétrogradé s'il y en a d'autres
        if (memberToUpdate.isOwner() && newRole != ProjectRole.OWNER) {
            long ownerCount = projectMemberRepository.findByProjectIdAndStatus(projectId, MemberStatus.ACTIVE)
                    .stream()
                    .filter(ProjectMember::isOwner)
                    .count();

            if (ownerCount <= 1) {
                throw new RuntimeException("Impossible de rétrograder le dernier propriétaire");
            }
        }

        ProjectRole oldRole = memberToUpdate.getRole();
        memberToUpdate.setRole(newRole);
        ProjectMember saved = projectMemberRepository.save(memberToUpdate);

        messageService.sendMemberRoleChanged(
                memberToUpdate.getUser(),
                requester.getUser(),
                memberToUpdate.getProject(),
                oldRole.name(),
                newRole.name()
        );

        return projectMemberMapper.toDTO(saved);
    }

    // ✅ NOUVEAU : Promouvoir en OWNER (au lieu de transférer)
    public ProjectMemberDTO promoteToOwner(Long projectId, Long memberUserId, Long requesterId) {
        ProjectMember requester = projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas membre de ce projet"));

        if (!requester.isOwner()) {
            throw new RuntimeException("Seul un propriétaire peut promouvoir quelqu'un comme propriétaire");
        }

        ProjectMember memberToPromote = projectMemberRepository.findByProjectIdAndUserId(projectId, memberUserId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        if (!memberToPromote.isActive()) {
            throw new RuntimeException("Le membre doit être actif pour être promu");
        }

        if (memberToPromote.isOwner()) {
            throw new RuntimeException("Ce membre est déjà propriétaire");
        }

        Project project = memberToPromote.getProject();
        ProjectRole oldRole = memberToPromote.getRole();

        memberToPromote.setRole(ProjectRole.OWNER);
        ProjectMember saved = projectMemberRepository.save(memberToPromote);

        // Message au nouveau propriétaire
        messageService.sendOwnershipReceived(memberToPromote.getUser(), requester.getUser(), project);

        return projectMemberMapper.toDTO(saved);
    }

    // ✅ GARDER pour compatibilité mais maintenant ça promeut simplement
    @Deprecated
    public void transferOwnership(Long projectId, Long newOwnerId, Long currentOwnerId) {
        promoteToOwner(projectId, newOwnerId, currentOwnerId);
    }

    // ========================================
    // UTILITAIRES
    // ========================================

    public List<ProjectMemberDTO> getPendingInvitations(Long userId) {
        return projectMemberRepository.findPendingInvitationsForUser(userId).stream()
                .map(projectMemberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public boolean canAccessProject(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(member -> member.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    public ProjectRole getUserRoleInProject(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ProjectMember::getRole)
                .orElseThrow(() -> new RuntimeException("Utilisateur non membre du projet"));
    }

    public ProjectMember createOwnerMembership(Project project, User owner) {
        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(ProjectRole.OWNER)
                .status(MemberStatus.ACTIVE)
                .build();
        ownerMember.setAcceptedAt(ownerMember.getJoinedAt());
        return projectMemberRepository.save(ownerMember);
    }
}