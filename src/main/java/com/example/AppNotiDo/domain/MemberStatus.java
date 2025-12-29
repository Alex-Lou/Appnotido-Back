package com.example.AppNotiDo.domain;

public enum MemberStatus {
    PENDING,    // Invitation envoyée, en attente d'acceptation
    ACTIVE,     // Membre actif
    DECLINED,   // Invitation refusée
    REMOVED     // Retiré du projet
}