package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.MemberStatus;
import com.example.AppNotiDo.domain.Project;
import com.example.AppNotiDo.domain.ProjectMember;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.ProjectNotFoundException;
import com.example.AppNotiDo.repository.ProjectMemberRepository;
import com.example.AppNotiDo.repository.ProjectRepository;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;
    private final ProjectMemberService projectMemberService;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository,
                          SecurityUtils securityUtils, ProjectMemberService projectMemberService,
                          ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
        this.projectMemberService = projectMemberService;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public Project createProject(Project project) {
        User currentUser = securityUtils.getCurrentUser();
        project.setUser(currentUser);

        Integer maxOrder = projectRepository.findMaxDisplayOrderByUserId(currentUser.getId());
        project.setDisplayOrder(maxOrder + 1);

        if (project.getColor() == null || project.getColor().isEmpty()) {
            project.setColor("#3B82F6");
        }
        if (project.getIcon() == null || project.getIcon().isEmpty()) {
            project.setIcon("folder");
        }
        if (project.getIsArchived() == null) {
            project.setIsArchived(false);
        }

        Project savedProject = projectRepository.save(project);

        projectMemberService.createOwnerMembership(savedProject, currentUser);

        return savedProject;
    }

    public List<Project> getAllProjects() {
        User currentUser = securityUtils.getCurrentUser();

        // Récupérer tous les projets où l'utilisateur est membre ACTIF
        List<ProjectMember> memberships = projectMemberRepository.findByUserIdAndStatus(
                currentUser.getId(),
                MemberStatus.ACTIVE
        );

        return memberships.stream()
                .map(ProjectMember::getProject)
                .filter(project -> !project.getIsArchived())
                .sorted((p1, p2) -> Integer.compare(p1.getDisplayOrder(), p2.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    public List<Project> getAllProjectsIncludingArchived() {
        User currentUser = securityUtils.getCurrentUser();

        List<ProjectMember> memberships = projectMemberRepository.findByUserIdAndStatus(
                currentUser.getId(),
                MemberStatus.ACTIVE
        );

        return memberships.stream()
                .map(ProjectMember::getProject)
                .sorted((p1, p2) -> Integer.compare(p1.getDisplayOrder(), p2.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    public List<Project> getArchivedProjects() {
        User currentUser = securityUtils.getCurrentUser();

        List<ProjectMember> memberships = projectMemberRepository.findByUserIdAndStatus(
                currentUser.getId(),
                MemberStatus.ACTIVE
        );

        return memberships.stream()
                .map(ProjectMember::getProject)
                .filter(Project::getIsArchived)
                .sorted((p1, p2) -> p2.getUpdatedAt().compareTo(p1.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public Project getProjectById(Long id) {
        User currentUser = securityUtils.getCurrentUser();

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projet non trouvé avec l'id: " + id));

        // Vérifier que l'utilisateur a accès via membership
        if (!projectMemberService.canAccessProject(id, currentUser.getId())) {
            throw new ProjectNotFoundException("Accès refusé à ce projet");
        }

        return project;
    }

    @Transactional
    public Project updateProject(Long id, Project updatedProject) {
        Project existingProject = getProjectById(id);

        if (updatedProject.getName() != null) {
            existingProject.setName(updatedProject.getName());
        }
        if (updatedProject.getDescription() != null) {
            existingProject.setDescription(updatedProject.getDescription());
        }
        if (updatedProject.getColor() != null) {
            existingProject.setColor(updatedProject.getColor());
        }
        if (updatedProject.getIcon() != null) {
            existingProject.setIcon(updatedProject.getIcon());
        }
        if (updatedProject.getDisplayOrder() != null) {
            existingProject.setDisplayOrder(updatedProject.getDisplayOrder());
        }

        return projectRepository.save(existingProject);
    }

    @Transactional
    public Project archiveProject(Long id) {
        Project project = getProjectById(id);
        project.setIsArchived(true);
        return projectRepository.save(project);
    }

    @Transactional
    public Project unarchiveProject(Long id) {
        Project project = getProjectById(id);
        project.setIsArchived(false);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);

        taskRepository.findByProjectId(id).forEach(task -> {
            task.setProject(null);
            taskRepository.save(task);
        });

        projectRepository.delete(project);
    }

    @Transactional
    public void deleteProjectWithTasks(Long id) {
        Project project = getProjectById(id);

        taskRepository.deleteByProjectId(id);

        projectRepository.delete(project);
    }

    public List<Project> searchProjects(String query) {
        User currentUser = securityUtils.getCurrentUser();

        List<ProjectMember> memberships = projectMemberRepository.findByUserIdAndStatus(
                currentUser.getId(),
                MemberStatus.ACTIVE
        );

        return memberships.stream()
                .map(ProjectMember::getProject)
                .filter(project -> !project.getIsArchived())
                .filter(project -> project.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public long countProjects() {
        User currentUser = securityUtils.getCurrentUser();

        List<ProjectMember> memberships = projectMemberRepository.findByUserIdAndStatus(
                currentUser.getId(),
                MemberStatus.ACTIVE
        );

        return memberships.stream()
                .map(ProjectMember::getProject)
                .filter(project -> !project.getIsArchived())
                .count();
    }

    @Transactional
    public void reorderProjects(List<Long> projectIds) {
        User currentUser = securityUtils.getCurrentUser();

        for (int i = 0; i < projectIds.size(); i++) {
            Long projectId = projectIds.get(i);
            final int order = i;  // Variable finale pour la lambda

            if (projectMemberService.canAccessProject(projectId, currentUser.getId())) {
                projectRepository.findById(projectId).ifPresent(project -> {
                    project.setDisplayOrder(order);
                    projectRepository.save(project);
                });
            }
        }
    }

}
