package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.TaskDTO;
import com.example.AppNotiDo.mapper.TaskMapper;
import com.example.AppNotiDo.service.TaskService;
import com.example.AppNotiDo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tasks", description = "API de gestion des tâches")
@RestController
@RequestMapping("/api/tasks")
public class TaskController{

    private final TaskService taskService;
    private final UserService userService; // ← AJOUT

    public TaskController(TaskService taskService, UserService userService){
        this.taskService = taskService;
        this.userService = userService; // ← AJOUT
    }

    @Operation(
            summary = "Créer une nouvelle tâche",
            description = "Crée une tâche avec un titre obligatoire. Le status par défaut est TODO et la priorité MEDIUM."
    )
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody Task task){
        Task createdTask = taskService.createTask(task);
        TaskDTO dto = TaskMapper.toDTO(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Récupérer toutes les tâches avec pagination")
    @GetMapping
    public Page<TaskDTO> getAllTasks(
            @Parameter(description = "Numéro de la page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Task> taskPage = taskService.getAllTasks(page, size);
        return taskPage.map(TaskMapper::toDTO);
    }

    @Operation(summary = "Récupérer une tâche par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche trouvée"),
            @ApiResponse(responseCode = "404", description = "Tâche introuvable")
    })
    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id){
        Task task = taskService.getTaskById(id);
        return TaskMapper.toDTO(task);
    }

    @Operation(summary = "Effacer une tâche existante")
    @DeleteMapping("/{id}")
    public void deleteTaskById(
            @Parameter(description = "ID de la tâche à supprimer") @PathVariable Long id
    ){
        taskService.deleteTask(id);
    }

    @Operation(summary = "Mettre à jour une tâche existante")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @Parameter(description = "ID de la tâche à modifier") @PathVariable Long id,
            @Valid @RequestBody TaskDTO taskDTO
    ){
        // Récupérer la tâche existante
        Task existingTask = taskService.getTaskById(id);

        // Mettre à jour UNIQUEMENT les champs fournis dans le DTO
        if (taskDTO.getTitle() != null) {
            existingTask.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            existingTask.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getStatus() != null) {
            existingTask.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
        }
        if (taskDTO.getPriority() != null) {
            existingTask.setPriority(TaskPriority.valueOf(taskDTO.getPriority()));
        }
        if (taskDTO.getDueDate() != null) {
            existingTask.setDueDate(taskDTO.getDueDate());
        }
        if (taskDTO.getEstimatedDuration() != null) {
            existingTask.setEstimatedDuration(taskDTO.getEstimatedDuration());
        }
        if (taskDTO.getReminderMinutes() != null) {
            existingTask.setReminderMinutes(taskDTO.getReminderMinutes());
        }
        if (taskDTO.getNotified() != null) {
            existingTask.setNotified(taskDTO.getNotified());
        }
        existingTask.setLocked(taskDTO.isLocked());

        if (taskDTO.getTags() != null) {
            existingTask.setTags(taskDTO.getTags());
        }

        // ⬇️ NOUVEAUX CHAMPS TIMER
        if (taskDTO.getIsRunning() != null) {
            existingTask.setIsRunning(taskDTO.getIsRunning());
        }
        if (taskDTO.getStartedAt() != null) {
            existingTask.setStartedAt(taskDTO.getStartedAt());
        }
        if (taskDTO.getPausedAt() != null) {
            existingTask.setPausedAt(taskDTO.getPausedAt());
        }
        if (taskDTO.getTimeSpent() != null) {
            existingTask.setTimeSpent(taskDTO.getTimeSpent());
        }

        // Sauvegarder (garde l'user, createdAt, etc.)
        Task updatedTask = taskService.saveTask(existingTask);

        // Retourner le DTO complet
        TaskDTO responseDTO = TaskMapper.toDTO(updatedTask);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Filtrer par statut")
    @GetMapping("/filter-status")
    public List<Task> getTaskByStatus(
            @Parameter(description = "Statut des tâches à filtrer", example = "TODO")
            @RequestParam TaskStatus status
    ){
        return taskService.getTaskByStatus(status);
    }

    @Operation(summary = "Filtrer par priorité")
    @GetMapping("/filter-priority")
    public List<Task> getTaskByPriority(
            @Parameter(description = "Priorité des tâches à filtrer", example = "MEDIUM")
            @RequestParam TaskPriority priority
    ){
        return taskService.getTaskByPriority(priority);
    }

    @Operation(summary = "Filtrer par Statut et Priorité")
    @GetMapping("/filter-combined")
    public List<Task> getTaskByStatusAndPriority(@RequestParam TaskStatus status, @RequestParam TaskPriority priority){
        return taskService.getTaskByStatusAndPriority(status, priority);
    }

    @Operation(summary = "Récupérer les tâches d'un utilisateur")
    @GetMapping("/user/{userId}")
    public Page<TaskDTO> getTasksByUserId(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable Long userId,
            @Parameter(description = "Numéro de la page")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Task> taskPage = taskService.getTasksByUserId(userId, page, size);
        return taskPage.map(TaskMapper::toDTO);
    }

    @Operation(summary = "Démarrer le timer d'une tâche")
    @PostMapping("/{id}/start")
    public ResponseEntity<TaskDTO> startTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Task updated = taskService.startTask(id, user);
        return ResponseEntity.ok(TaskMapper.toDTO(updated));
    }

    @Operation(summary = "Mettre en pause le timer d'une tâche")
    @PostMapping("/{id}/pause")
    public ResponseEntity<TaskDTO> pauseTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Task updated = taskService.pauseTask(id, user);
        return ResponseEntity.ok(TaskMapper.toDTO(updated));
    }

    @Operation(summary = "Arrêter le timer d'une tâche (et la marquer comme DONE)")
    @PostMapping("/{id}/stop")
    public ResponseEntity<TaskDTO> stopTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Task updated = taskService.stopTask(id, user);
        return ResponseEntity.ok(TaskMapper.toDTO(updated));
    }

}
