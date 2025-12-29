package com.example.AppNotiDo.dto;

import com.example.AppNotiDo.domain.MemberStatus;
import com.example.AppNotiDo.domain.ProjectRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberDTO {

    private Long id;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private ProjectRole role;
    private MemberStatus status;
    private Long invitedById;
    private String invitedByUsername;
    private LocalDateTime joinedAt;
    private LocalDateTime acceptedAt;
}