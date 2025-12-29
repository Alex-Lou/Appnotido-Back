package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.GlobalRole;
import com.example.AppNotiDo.dto.AdminStatsDTO;
import com.example.AppNotiDo.dto.AdminStatsDTO.ProjectAdminDTO;
import com.example.AppNotiDo.dto.AdminStatsDTO.UserAdminDTO;
import com.example.AppNotiDo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin", description = "API d'administration (SUPER_ADMIN uniquement)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final com.example.AppNotiDo.repository.UserRepository userRepository;

    // ========================================
    // STATISTIQUES
    // ========================================

    @Operation(summary = "Récupérer les statistiques globales")
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getGlobalStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getGlobalStats());
    }

    // ========================================
    // GESTION DES UTILISATEURS
    // ========================================

    @Operation(summary = "Récupérer tous les utilisateurs (paginé)")
    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getAllUsers(page, size, search));
    }

    @Operation(summary = "Récupérer les détails d'un utilisateur")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserAdminDTO> getUserDetails(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    @Operation(summary = "Modifier le rôle global d'un utilisateur")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserAdminDTO> updateUserGlobalRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        GlobalRole newRole = GlobalRole.valueOf(request.get("globalRole"));
        return ResponseEntity.ok(adminService.updateUserGlobalRole(userId, newRole));
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer les projets d'un utilisateur")
    @GetMapping("/users/{userId}/projects")
    public ResponseEntity<List<ProjectAdminDTO>> getUserProjects(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getProjectsByUser(userId));
    }

    // ========================================
    // GESTION DES PROJETS
    // ========================================

    @Operation(summary = "Récupérer tous les projets (paginé)")
    @GetMapping("/projects")
    public ResponseEntity<Page<ProjectAdminDTO>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getAllProjects(page, size, search));
    }

    @Operation(summary = "Récupérer les détails d'un projet")
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectAdminDTO> getProjectDetails(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getProjectDetails(projectId));
    }

    @Operation(summary = "Récupérer les membres d'un projet")
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<List<UserAdminDTO>> getProjectMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        return ResponseEntity.ok(adminService.getMembersByProject(projectId));
    }

    @Operation(summary = "Supprimer un projet")
    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        adminService.requireSuperAdmin(currentUserId);

        adminService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // VÉRIFICATION ADMIN
    // ========================================

    @Operation(summary = "Vérifier si l'utilisateur est SUPER_ADMIN")
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkSuperAdmin(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = getCurrentUserId(userDetails);
        boolean isSuperAdmin = adminService.isSuperAdmin(currentUserId);

        return ResponseEntity.ok(Map.of("isSuperAdmin", isSuperAdmin));
    }

    // ========================================
    // UTILITAIRE
    // ========================================

    private Long getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }
}