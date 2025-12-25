package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.KanbanConfig;
import com.example.AppNotiDo.dto.KanbanConfigDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KanbanConfigMapper {

    public static KanbanConfigDTO toDTO(KanbanConfig config) {
        if (config == null) return getDefaultDTO();

        KanbanConfigDTO dto = new KanbanConfigDTO();
        dto.setId(config.getId());
        dto.setVisibleStatusColumns(stringToList(config.getVisibleStatusColumns()));
        dto.setActiveTagColumns(stringToList(config.getActiveTagColumns()));
        dto.setColumnsOrder(stringToList(config.getColumnsOrder()));

        return dto;
    }

    public static void updateFromDTO(KanbanConfig config, KanbanConfigDTO dto) {
        if (dto.getVisibleStatusColumns() != null) {
            config.setVisibleStatusColumns(listToString(dto.getVisibleStatusColumns()));
        }
        if (dto.getActiveTagColumns() != null) {
            config.setActiveTagColumns(listToString(dto.getActiveTagColumns()));
        }
        if (dto.getColumnsOrder() != null) {
            config.setColumnsOrder(listToString(dto.getColumnsOrder()));
        }
    }

    public static KanbanConfigDTO getDefaultDTO() {
        KanbanConfigDTO dto = new KanbanConfigDTO();
        // Utiliser ArrayList pour avoir des listes mutables
        dto.setVisibleStatusColumns(new ArrayList<>(Arrays.asList("TODO", "IN_PROGRESS", "DONE")));
        dto.setActiveTagColumns(new ArrayList<>());
        dto.setColumnsOrder(new ArrayList<>(Arrays.asList("TODO", "IN_PROGRESS", "DONE")));
        return dto;
    }

    private static List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            // Retourner une ArrayList mutable, pas Collections.emptyList()
            return new ArrayList<>();
        }
        // Utiliser Collectors.toList() dans un new ArrayList pour garantir la mutabilité
        return new ArrayList<>(
                Arrays.stream(str.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList())
        );
    }

    private static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }
}