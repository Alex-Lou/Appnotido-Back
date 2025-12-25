package com.example.AppNotiDo.dto;

import lombok.Data;

import java.util.List;

@Data
public class KanbanConfigDTO {

    private Long id;
    private List<String> visibleStatusColumns;
    private List<String> activeTagColumns;
    private List<String> columnsOrder;

}