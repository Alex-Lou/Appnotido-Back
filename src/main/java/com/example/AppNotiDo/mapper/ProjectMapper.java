package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.Project;
import com.example.AppNotiDo.dto.ProjectDTO;

public class ProjectMapper {

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