package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.ProjectRole;
import com.example.AppNotiDo.dto.ProjectMemberDTO;
import com.example.AppNotiDo.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final com.example.AppNotiDo.repository.UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProjectMemberDTO>> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProjectMemberDTO>> getActiveMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectMemberService.getActiveProjectMembers(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectMemberDTO> inviteMember(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long inviterId = getCurrentUserId(userDetails);
        String usernameOrEmail = request.get("usernameOrEmail");
        ProjectRole role = ProjectRole.valueOf(request.getOrDefault("role", "MEMBER"));

        ProjectMemberDTO member = projectMemberService.inviteMember(projectId, usernameOrEmail, role, inviterId);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ProjectMemberDTO> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long requesterId = getCurrentUserId(userDetails);
        ProjectRole newRole = ProjectRole.valueOf(request.get("role"));

        ProjectMemberDTO updated = projectMemberService.updateMemberRole(projectId, userId, newRole, requesterId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long requesterId = getCurrentUserId(userDetails);
        projectMemberService.removeMember(projectId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer-ownership")
    public ResponseEntity<Void> transferOwnership(
            @PathVariable Long projectId,
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentOwnerId = getCurrentUserId(userDetails);
        Long newOwnerId = request.get("newOwnerId");

        projectMemberService.transferOwnership(projectId, newOwnerId, currentOwnerId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }
}
