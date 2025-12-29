package com.example.AppNotiDo.domain;

public enum ProjectRole {
    OWNER,      // Créateur du projet - tous les droits
    ADMIN,      // Peut gérer les membres et toutes les tâches
    MEMBER,     // Peut créer/éditer des tâches
    VIEWER      // Lecture seule
}