package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.Subtask;
import com.example.AppNotiDo.dto.SubtaskDTO;

import java.util.List;
import java.util.stream.Collectors;

public class SubtaskMapper {

    public static SubtaskDTO toDTO(Subtask subtask) {
        if (subtask == null) return null;

        SubtaskDTO dto = new SubtaskDTO();
        dto.setId(subtask.getId());
        dto.setTitle(subtask.getTitle());
        dto.setCompleted(subtask.getCompleted());
        dto.setDisplayOrder(subtask.getDisplayOrder());
        dto.setCreatedAt(subtask.getCreatedAt());
        dto.setCompletedAt(subtask.getCompletedAt());

        if (subtask.getTask() != null) {
            dto.setTaskId(subtask.getTask().getId());
        }

        return dto;
    }

    public static Subtask toEntity(SubtaskDTO dto) {
        if (dto == null) return null;

        Subtask subtask = new Subtask();
        subtask.setId(dto.getId());
        subtask.setTitle(dto.getTitle());
        subtask.setCompleted(dto.getCompleted() != null ? dto.getCompleted() : false);
        subtask.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);

        return subtask;
    }

    public static List<SubtaskDTO> toDTOList(List<Subtask> subtasks) {
        if (subtasks == null) return List.of();
        return subtasks.stream()
                .map(SubtaskMapper::toDTO)
                .collect(Collectors.toList());
    }
}