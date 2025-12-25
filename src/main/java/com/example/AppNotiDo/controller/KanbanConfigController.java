package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.KanbanConfigDTO;
import com.example.AppNotiDo.service.KanbanConfigService;
import com.example.AppNotiDo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Kanban Config", description = "API de configuration du Kanban")
@RestController
@RequestMapping("/api/kanban-config")
@CrossOrigin(origins = "http://localhost:5173")
public class KanbanConfigController {

    private final KanbanConfigService kanbanConfigService;
    private final UserService userService;

    public KanbanConfigController(KanbanConfigService kanbanConfigService, UserService userService) {
        this.kanbanConfigService = kanbanConfigService;
        this.userService = userService;
    }

    @Operation(summary = "Récupérer la configuration Kanban de l'utilisateur")
    @GetMapping
    public ResponseEntity<KanbanConfigDTO> getConfig(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        KanbanConfigDTO config = kanbanConfigService.getConfigForUser(user);
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Sauvegarder la configuration Kanban")
    @PutMapping
    public ResponseEntity<KanbanConfigDTO> saveConfig(
            @RequestBody KanbanConfigDTO dto,
            Authentication authentication
    ) {
        User user = userService.getUserByUsername(authentication.getName());
        KanbanConfigDTO saved = kanbanConfigService.saveConfig(user, dto);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Ajouter une colonne tag")
    @PostMapping("/tags/{tag}")
    public ResponseEntity<KanbanConfigDTO> addTagColumn(
            @PathVariable String tag,
            Authentication authentication
    ) {
        User user = userService.getUserByUsername(authentication.getName());
        KanbanConfigDTO config = kanbanConfigService.addTagColumn(user, tag);
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Supprimer une colonne tag")
    @DeleteMapping("/tags/{tag}")
    public ResponseEntity<KanbanConfigDTO> removeTagColumn(
            @PathVariable String tag,
            Authentication authentication
    ) {
        User user = userService.getUserByUsername(authentication.getName());
        KanbanConfigDTO config = kanbanConfigService.removeTagColumn(user, tag);
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Toggle visibilité d'une colonne status")
    @PatchMapping("/status/{status}/toggle")
    public ResponseEntity<KanbanConfigDTO> toggleStatusColumn(
            @PathVariable String status,
            Authentication authentication
    ) {
        User user = userService.getUserByUsername(authentication.getName());
        KanbanConfigDTO config = kanbanConfigService.toggleStatusColumn(user, status);
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Réinitialiser la configuration par défaut")
    @PostMapping("/reset")
    public ResponseEntity<KanbanConfigDTO> resetConfig(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        KanbanConfigDTO config = kanbanConfigService.resetToDefault(user);
        return ResponseEntity.ok(config);
    }
}