package com.example.AppNotiDo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @NotBlank(message = "Le nom d'affichage ne peut pas être vide")
    @Size(max = 100, message = "Le nom d'affichage ne peut pas dépasser 100 caractères")
    private String displayName;
}
