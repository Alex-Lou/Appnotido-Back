package com.example.AppNotiDo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {

    // Stats globales
    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long totalTasksCompleted;
    private long totalProjectMembers;

    // Stats récentes (30 derniers jours)
    private long newUsersLast30Days;
    private long newProjectsLast30Days;
    private long newTasksLast30Days;

    // Listes pour la vue admin
    private List<UserAdminDTO> recentUsers;
    private List<ProjectAdminDTO> recentProjects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAdminDTO {
        private Long id;
        private String username;
        private String email;
        private String displayName;
        private String globalRole;
        private String createdAt;
        private long projectCount;
        private long taskCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAdminDTO {
        private Long id;
        private String name;
        private String ownerUsername;
        private String color;
        private boolean isArchived;
        private String createdAt;
        private long memberCount;
        private long taskCount;
    }
}