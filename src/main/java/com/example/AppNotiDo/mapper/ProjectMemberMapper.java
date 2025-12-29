package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.ProjectMember;
import com.example.AppNotiDo.dto.ProjectMemberDTO;
import org.springframework.stereotype.Component;

@Component
public class ProjectMemberMapper {

    public ProjectMemberDTO toDTO(ProjectMember entity) {
        if (entity == null) return null;

        return ProjectMemberDTO.builder()
                .id(entity.getId())
                .projectId(entity.getProject().getId())
                .projectName(entity.getProject().getName())
                .userId(entity.getUser().getId())
                .username(entity.getUser().getUsername())
                .displayName(entity.getUser().getDisplayName())
                .email(entity.getUser().getEmail())
                .role(entity.getRole())
                .status(entity.getStatus())
                .invitedById(entity.getInvitedBy() != null ? entity.getInvitedBy().getId() : null)
                .invitedByUsername(entity.getInvitedBy() != null ? entity.getInvitedBy().getUsername() : null)
                .joinedAt(entity.getJoinedAt())
                .acceptedAt(entity.getAcceptedAt())
                .build();
    }
}