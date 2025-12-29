package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.Project;
import com.example.AppNotiDo.dto.ProjectDTO;
import com.example.AppNotiDo.mapper.ProjectMapper;
import com.example.AppNotiDo.service.ProjectService;
import com.example.AppNotiDo.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Projects", description = "API de gestion des projets")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final com.example.AppNotiDo.repository.UserRepository userRepository;
    private final ProjectMemberService projectMemberService;

    public ProjectController(ProjectService projectService,
                             com.example.AppNotiDo.repository.UserRepository userRepository,
                             ProjectMemberService projectMemberService) {
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.projectMemberService = projectMemberService;
    }

    @Operation(summary = "Créer un nouveau projet")
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(
            @Valid @RequestBody ProjectDTO projectDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);

        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setColor(projectDTO.getColor());
        project.setIcon(projectDTO.getIcon());

        Project createdProject = projectService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectMapper.toDTO(createdProject, currentUserId, projectMemberService));
    }

    @Operation(summary = "Récupérer tous les projets")
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects(
            @Parameter(description = "Inclure les projets archivés")
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = getCurrentUserId(userDetails);

        List<Project> projects = includeArchived
                ? projectService.getAllProjectsIncludingArchived()
                : projectService.getAllProjects();

        List<ProjectDTO> dtos = projects.stream()
                .map(project -> ProjectMapper.toDTO(project, currentUserId, projectMemberService))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Récupérer les projets archivés")
    @GetMapping("/archived")
    public ResponseEntity<List<ProjectDTO>> getArchivedProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);

        List<Project> projects = projectService.getArchivedProjects();
        List<ProjectDTO> dtos = projects.stream()
                .map(project -> ProjectMapper.toDTO(project, currentUserId, projectMemberService))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Récupérer un projet par ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);

        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(ProjectMapper.toDTO(project, currentUserId, projectMemberService));
    }

    @Operation(summary = "Mettre à jour un projet")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO projectDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = getCurrentUserId(userDetails);

        Project projectToUpdate = new Project();
        projectToUpdate.setName(projectDTO.getName());
        projectToUpdate.setDescription(projectDTO.getDescription());
        projectToUpdate.setColor(projectDTO.getColor());
        projectToUpdate.setIcon(projectDTO.getIcon());
        projectToUpdate.setDisplayOrder(projectDTO.getDisplayOrder());

        Project updatedProject = projectService.updateProject(id, projectToUpdate);
        return ResponseEntity.ok(ProjectMapper.toDTO(updatedProject, currentUserId, projectMemberService));
    }

    @Operation(summary = "Archiver un projet")
    @PostMapping("/{id}/archive")
    public ResponseEntity<ProjectDTO> archiveProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);

        Project project = projectService.archiveProject(id);
        return ResponseEntity.ok(ProjectMapper.toDTO(project, currentUserId, projectMemberService));
    }

    @Operation(summary = "Désarchiver un projet")
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<ProjectDTO> unarchiveProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);

        Project project = projectService.unarchiveProject(id);
        return ResponseEntity.ok(ProjectMapper.toDTO(project, currentUserId, projectMemberService));
    }

    @Operation(summary = "Supprimer un projet (garde les tâches)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer un projet avec toutes ses tâches")
    @DeleteMapping("/{id}/with-tasks")
    public ResponseEntity<Void> deleteProjectWithTasks(@PathVariable Long id) {
        projectService.deleteProjectWithTasks(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rechercher des projets")
    @GetMapping("/search")
    public ResponseEntity<List<ProjectDTO>> searchProjects(
            @Parameter(description = "Terme de recherche")
            @RequestParam String q,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = getCurrentUserId(userDetails);

        List<Project> projects = projectService.searchProjects(q);
        List<ProjectDTO> dtos = projects.stream()
                .map(project -> ProjectMapper.toDTO(project, currentUserId, projectMemberService))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Compter les projets")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countProjects() {
        long count = projectService.countProjects();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Réordonner les projets")
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderProjects(@RequestBody List<Long> projectIds) {
        projectService.reorderProjects(projectIds);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }
}
