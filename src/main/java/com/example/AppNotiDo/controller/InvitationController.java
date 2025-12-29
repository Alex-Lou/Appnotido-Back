package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.dto.ProjectMemberDTO;
import com.example.AppNotiDo.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final ProjectMemberService projectMemberService;
    private final com.example.AppNotiDo.repository.UserRepository userRepository;

    /**
     * GET /api/invitations - Mes invitations en attente
     */
    @GetMapping
    public ResponseEntity<List<ProjectMemberDTO>> getMyPendingInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectMemberService.getPendingInvitations(userId));
    }

    /**
     * POST /api/invitations/{memberId}/accept - Accepter une invitation
     */
    @PostMapping("/{memberId}/accept")
    public ResponseEntity<ProjectMemberDTO> acceptInvitation(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        ProjectMemberDTO accepted = projectMemberService.acceptInvitation(memberId, userId);
        return ResponseEntity.ok(accepted);
    }

    /**
     * POST /api/invitations/{memberId}/decline - Refuser une invitation
     */
    @PostMapping("/{memberId}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        projectMemberService.declineInvitation(memberId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
                .getId();
    }
}