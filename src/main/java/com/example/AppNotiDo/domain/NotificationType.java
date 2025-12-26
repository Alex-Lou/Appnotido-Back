package com.example.AppNotiDo.domain;

public enum NotificationType {
    // Échéances
    REMINDER,           // Rappel avant échéance
    DEADLINE,           // Échéance atteinte
    OVERDUE,            // Tâche en retard

    // Actions sur tâches
    TASK_CREATED,       // Tâche créée
    TASK_UPDATED,       // Tâche modifiée
    TASK_DELETED,       // Tâche supprimée
    TASK_COMPLETED,     // Tâche terminée
    TASK_REACTIVATED,   // Tâche réactivée/déplacée à aujourd'hui

    // Timer
    TIMER_STARTED,      // Timer démarré
    TIMER_PAUSED,       // Timer en pause
    TIMER_STOPPED,      // Timer arrêté

    // Statut
    STATUS_CHANGED,     // Changement de statut
    PRIORITY_CHANGED,   // Changement de priorité

    // Système
    SYSTEM              // Notification système
}