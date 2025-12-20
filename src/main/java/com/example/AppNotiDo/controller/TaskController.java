package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.Task;
import com.example.AppNotiDo.domain.TaskPriority;
import com.example.AppNotiDo.domain.TaskStatus;
import com.example.AppNotiDo.dto.TaskDTO;
import com.example.AppNotiDo.mapper.TaskMapper;
import com.example.AppNotiDo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tasks", description = "API de gestion des tâches")
@RestController
@RequestMapping("/api/tasks")
public class TaskController{

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
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
            @Valid @RequestBody TaskDTO taskDTO  // Reçoit un TaskDTO
    ){
        // Convertir TaskDTO en Task
        Task task = TaskMapper.toEntity(taskDTO);

        // Mettre à jour
        Task updatedTask = taskService.updateTask(id, task);

        // Retourner le DTO
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

}
