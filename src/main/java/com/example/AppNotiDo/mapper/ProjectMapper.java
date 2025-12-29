package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.Project;
import com.example.AppNotiDo.domain.ProjectRole;
import com.example.AppNotiDo.dto.ProjectDTO;
import com.example.AppNotiDo.service.ProjectMemberService;

public class ProjectMapper {

    // Version avec rôle (utilisée par les endpoints API)
    public static ProjectDTO toDTO(Project project, Long userId, ProjectMemberService memberService) {
        if (project == null) return null;

        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setColor(project.getColor());
        dto.setIcon(project.getIcon());
        dto.setIsArchived(project.getIsArchived());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        // Stats
        dto.setTaskCount(project.getTaskCount());
        dto.setCompletedTaskCount(project.getCompletedTaskCount());
        dto.setPendingTaskCount(project.getPendingTaskCount());

        // Récupérer le rôle de l'utilisateur
        try {
            ProjectRole role = memberService.getUserRoleInProject(project.getId(), userId);
            dto.setUserRole(role);
        } catch (Exception e) {
            // Si pas de membership trouvé (anciens projets), mettre OWNER par défaut
            dto.setUserRole(ProjectRole.OWNER);
        }

        return dto;
    }

    // Version sans rôle (pour compatibilité interne)
    public static ProjectDTO toDTO(Project project) {
        if (project == null) return null;

        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setColor(project.getColor());
        dto.setIcon(project.getIcon());
        dto.setIsArchived(project.getIsArchived());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        // Stats
        dto.setTaskCount(project.getTaskCount());
        dto.setCompletedTaskCount(project.getCompletedTaskCount());
        dto.setPendingTaskCount(project.getPendingTaskCount());

        return dto;
    }

    public static Project toEntity(ProjectDTO dto) {
        if (dto == null) return null;

        Project project = new Project();
        project.setId(dto.getId());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setColor(dto.getColor());
        project.setIcon(dto.getIcon());
        project.setIsArchived(dto.getIsArchived());
        project.setDisplayOrder(dto.getDisplayOrder());

        return project;
    }
}
