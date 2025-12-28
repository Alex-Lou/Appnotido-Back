package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.Project;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.ProjectNotFoundException;
import com.example.AppNotiDo.repository.ProjectRepository;
import com.example.AppNotiDo.repository.TaskRepository;
import com.example.AppNotiDo.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository, SecurityUtils securityUtils) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    // Créer un nouveau projet
    @Transactional
    public Project createProject(Project project) {
        User currentUser = securityUtils.getCurrentUser();
        project.setUser(currentUser);

        // Définir l'ordre d'affichage
        Integer maxOrder = projectRepository.findMaxDisplayOrderByUserId(currentUser.getId());
        project.setDisplayOrder(maxOrder + 1);

        // Valeurs par défaut
        if (project.getColor() == null || project.getColor().isEmpty()) {
            project.setColor("#3B82F6");
        }
        if (project.getIcon() == null || project.getIcon().isEmpty()) {
            project.setIcon("folder");
        }
        if (project.getIsArchived() == null) {
            project.setIsArchived(false);
        }

        return projectRepository.save(project);
    }

    // Récupérer tous les projets de l'utilisateur (non archivés)
    public List<Project> getAllProjects() {
        User currentUser = securityUtils.getCurrentUser();
        return projectRepository.findByUserIdAndIsArchivedFalseOrderByDisplayOrderAsc(currentUser.getId());
    }

    // Récupérer tous les projets incluant les archivés
    public List<Project> getAllProjectsIncludingArchived() {
        User currentUser = securityUtils.getCurrentUser();
        return projectRepository.findByUserIdOrderByDisplayOrderAsc(currentUser.getId());
    }

    // Récupérer les projets archivés
    public List<Project> getArchivedProjects() {
        User currentUser = securityUtils.getCurrentUser();
        return projectRepository.findByUserIdAndIsArchivedTrueOrderByUpdatedAtDesc(currentUser.getId());
    }

    // Récupérer un projet par ID
    public Project getProjectById(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        return projectRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ProjectNotFoundException("Projet non trouvé avec l'id: " + id));
    }

    // Mettre à jour un projet
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

    // Archiver un projet
    @Transactional
    public Project archiveProject(Long id) {
        Project project = getProjectById(id);
        project.setIsArchived(true);
        return projectRepository.save(project);
    }

    // Désarchiver un projet
    @Transactional
    public Project unarchiveProject(Long id) {
        Project project = getProjectById(id);
        project.setIsArchived(false);
        return projectRepository.save(project);
    }

    // Supprimer un projet
    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);

        // Dissocier les tâches du projet (ne pas les supprimer)
        taskRepository.findByProjectId(id).forEach(task -> {
            task.setProject(null);
            taskRepository.save(task);
        });

        projectRepository.delete(project);
    }

    // Supprimer un projet avec ses tâches
    @Transactional
    public void deleteProjectWithTasks(Long id) {
        Project project = getProjectById(id);

        // Supprimer toutes les tâches du projet
        taskRepository.deleteByProjectId(id);

        projectRepository.delete(project);
    }

    // Rechercher des projets
    public List<Project> searchProjects(String query) {
        User currentUser = securityUtils.getCurrentUser();
        return projectRepository.findByUserIdAndNameContainingIgnoreCaseAndIsArchivedFalse(
                currentUser.getId(), query);
    }

    // Compter les projets
    public long countProjects() {
        User currentUser = securityUtils.getCurrentUser();
        return projectRepository.countByUserIdAndIsArchivedFalse(currentUser.getId());
    }

    // Réordonner les projets
    @Transactional
    public void reorderProjects(List<Long> projectIds) {
        User currentUser = securityUtils.getCurrentUser();

        for (int i = 0; i < projectIds.size(); i++) {
            Long projectId = projectIds.get(i);
            projectRepository.findByIdAndUserId(projectId, currentUser.getId())
                    .ifPresent(project -> {
                        project.setDisplayOrder(projectIds.indexOf(projectId));
                        projectRepository.save(project);
                    });
        }
    }
}