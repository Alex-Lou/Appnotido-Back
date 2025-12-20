package com.example.AppNotiDo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 100, message = "Le nom d'affichage ne peut pas dépasser 100 caractères")
    private String displayName;

    @Email(message = "Email invalide")
    private String email;
}
