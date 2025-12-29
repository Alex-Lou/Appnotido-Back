package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.*;
import com.example.AppNotiDo.dto.AdminStatsDTO;
import com.example.AppNotiDo.dto.AdminStatsDTO.UserAdminDTO;
import com.example.AppNotiDo.dto.AdminStatsDTO.ProjectAdminDTO;
import com.example.AppNotiDo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========================================
    // VÉRIFICATION SUPER_ADMIN
    // ========================================

    public boolean isSuperAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(User::isSuperAdmin)
                .orElse(false);
    }

    public void requireSuperAdmin(Long userId) {
        if (!isSuperAdmin(userId)) {
            throw new RuntimeException("Accès refusé : vous devez être SUPER_ADMIN");
        }
    }

    // ========================================
    // STATISTIQUES GLOBALES
    // ========================================

    public AdminStatsDTO getGlobalStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return AdminStatsDTO.builder()
                .totalUsers(userRepository.count())
                .totalProjects(projectRepository.count())
                .totalTasks(taskRepository.count())
                .totalTasksCompleted(taskRepository.countByStatus(TaskStatus.DONE))
                .totalProjectMembers(projectMemberRepository.count())
                .newUsersLast30Days(userRepository.countByCreatedAtAfter(thirtyDaysAgo))
                .newProjectsLast30Days(projectRepository.countByCreatedAtAfter(thirtyDaysAgo))
                .newTasksLast30Days(taskRepository.countByCreatedAtAfter(thirtyDaysAgo))
                .build();
    }

    // ========================================
    // GESTION DES UTILISATEURS
    // ========================================

    public Page<UserAdminDTO> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::toUserAdminDTO);
    }

    public UserAdminDTO getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return toUserAdminDTO(user);
    }

    @Transactional
    public UserAdminDTO updateUserGlobalRole(Long userId, GlobalRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setGlobalRole(newRole);
        User saved = userRepository.save(user);
        return toUserAdminDTO(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.isSuperAdmin()) {
            throw new RuntimeException("Impossible de supprimer un SUPER_ADMIN");
        }

        userRepository.delete(user);
    }

    // ========================================
    // GESTION DES PROJETS
    // ========================================

    public Page<ProjectAdminDTO> getAllProjects(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Project> projects;
        if (search != null && !search.trim().isEmpty()) {
            projects = projectRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            projects = projectRepository.findAll(pageable);
        }

        return projects.map(this::toProjectAdminDTO);
    }

    public ProjectAdminDTO getProjectDetails(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return toProjectAdminDTO(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // Supprimer les membres du projet
        projectMemberRepository.deleteByProjectId(projectId);

        // Dissocier les tâches
        taskRepository.findByProjectId(projectId).forEach(task -> {
            task.setProject(null);
            taskRepository.save(task);
        });

        projectRepository.delete(project);
    }

    // ========================================
    // GESTION DES MEMBRES (vue globale)
    // ========================================

    public List<ProjectAdminDTO> getProjectsByUser(Long userId) {
        List<ProjectMember> memberships = projectMemberRepository.findByUserIdAndStatus(
                userId, MemberStatus.ACTIVE);

        return memberships.stream()
                .map(pm -> toProjectAdminDTO(pm.getProject()))
                .collect(Collectors.toList());
    }

    public List<UserAdminDTO> getMembersByProject(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectIdAndStatus(
                projectId, MemberStatus.ACTIVE);

        return members.stream()
                .map(pm -> toUserAdminDTO(pm.getUser()))
                .collect(Collectors.toList());
    }

    // ========================================
    // MAPPERS
    // ========================================

    private UserAdminDTO toUserAdminDTO(User user) {
        long projectCount = projectMemberRepository.findByUserIdAndStatus(user.getId(), MemberStatus.ACTIVE).size();
        long taskCount = taskRepository.countByUserId(user.getId());

        return UserAdminDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .globalRole(user.getGlobalRole().name())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : null)
                .projectCount(projectCount)
                .taskCount(taskCount)
                .build();
    }

    private ProjectAdminDTO toProjectAdminDTO(Project project) {
        long memberCount = projectMemberRepository.countByProjectIdAndStatus(project.getId(), MemberStatus.ACTIVE);
        long taskCount = project.getTasks() != null ? project.getTasks().size() : 0;

        // Trouver le premier OWNER
        String ownerUsername = projectMemberRepository.findByProjectIdAndStatus(project.getId(), MemberStatus.ACTIVE)
                .stream()
                .filter(ProjectMember::isOwner)
                .findFirst()
                .map(pm -> pm.getUser().getUsername())
                .orElse("Inconnu");

        return ProjectAdminDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .ownerUsername(ownerUsername)
                .color(project.getColor())
                .isArchived(project.getIsArchived())
                .createdAt(project.getCreatedAt() != null ? project.getCreatedAt().format(DATE_FORMATTER) : null)
                .memberCount(memberCount)
                .taskCount(taskCount)
                .build();
    }
}