package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.Subtask;
import com.example.AppNotiDo.dto.SubtaskDTO;
import com.example.AppNotiDo.mapper.SubtaskMapper;
import com.example.AppNotiDo.service.SubtaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SubtaskController {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    // GET /api/tasks/{taskId}/subtasks - Récupérer toutes les sous-tâches d'une tâche
    @GetMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<List<SubtaskDTO>> getSubtasksByTaskId(@PathVariable Long taskId) {
        List<Subtask> subtasks = subtaskService.getSubtasksByTaskId(taskId);
        return ResponseEntity.ok(SubtaskMapper.toDTOList(subtasks));
    }

    // POST /api/tasks/{taskId}/subtasks - Créer une sous-tâche
    @PostMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<SubtaskDTO> createSubtask(
            @PathVariable Long taskId,
            @Valid @RequestBody SubtaskDTO subtaskDTO) {

        Subtask subtask = SubtaskMapper.toEntity(subtaskDTO);
        Subtask created = subtaskService.createSubtask(taskId, subtask);
        return ResponseEntity.status(HttpStatus.CREATED).body(SubtaskMapper.toDTO(created));
    }

    // PUT /api/subtasks/{subtaskId} - Mettre à jour une sous-tâche
    @PutMapping("/subtasks/{subtaskId}")
    public ResponseEntity<SubtaskDTO> updateSubtask(
            @PathVariable Long subtaskId,
            @Valid @RequestBody SubtaskDTO subtaskDTO) {

        Subtask subtask = SubtaskMapper.toEntity(subtaskDTO);
        Subtask updated = subtaskService.updateSubtask(subtaskId, subtask);
        return ResponseEntity.ok(SubtaskMapper.toDTO(updated));
    }

    // PATCH /api/subtasks/{subtaskId}/toggle - Toggle le statut d'une sous-tâche
    @PatchMapping("/subtasks/{subtaskId}/toggle")
    public ResponseEntity<SubtaskDTO> toggleSubtask(@PathVariable Long subtaskId) {
        Subtask toggled = subtaskService.toggleSubtask(subtaskId);
        return ResponseEntity.ok(SubtaskMapper.toDTO(toggled));
    }

    // DELETE /api/subtasks/{subtaskId} - Supprimer une sous-tâche
    @DeleteMapping("/subtasks/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(@PathVariable Long subtaskId) {
        subtaskService.deleteSubtask(subtaskId);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/tasks/{taskId}/subtasks/reorder - Réordonner les sous-tâches
    @PutMapping("/tasks/{taskId}/subtasks/reorder")
    public ResponseEntity<Void> reorderSubtasks(
            @PathVariable Long taskId,
            @RequestBody List<Long> subtaskIds) {

        subtaskService.reorderSubtasks(taskId, subtaskIds);
        return ResponseEntity.ok().build();
    }

    // GET /api/tasks/{taskId}/subtasks/stats - Stats des sous-tâches
    @GetMapping("/tasks/{taskId}/subtasks/stats")
    public ResponseEntity<Map<String, Object>> getSubtaskStats(@PathVariable Long taskId) {
        long total = subtaskService.countSubtasks(taskId);
        long completed = subtaskService.countCompletedSubtasks(taskId);
        double progress = total > 0 ? (completed * 100.0 / total) : 0.0;

        Map<String, Object> stats = Map.of(
                "total", total,
                "completed", completed,
                "pending", total - completed,
                "progress", progress
        );

        return ResponseEntity.ok(stats);
    }
}