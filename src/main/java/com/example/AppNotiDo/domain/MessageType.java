package com.example.AppNotiDo.domain;

public enum MessageType {
    // Invitations
    INVITATION_RECEIVED,
    INVITATION_ACCEPTED,
    INVITATION_DECLINED,

    // Membres
    MEMBER_JOINED,
    MEMBER_REMOVED,
    MEMBER_LEFT,
    MEMBER_ROLE_CHANGED,

    // Propriété
    OWNERSHIP_TRANSFERRED,
    OWNERSHIP_RECEIVED,

    // Projet
    PROJECT_ARCHIVED,
    PROJECT_DELETED,

    // Général
    SYSTEM_MESSAGE
}