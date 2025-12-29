package com.example.AppNotiDo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    // ✅ NOUVEAU : Rôle global (SUPER_ADMIN ou USER)
    @Enumerated(EnumType.STRING)
    @Column(name = "global_role", nullable = false)
    private GlobalRole globalRole = GlobalRole.USER;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "theme")
    private String theme = "light";

    @Column(name = "display_name", length = 100)
    private String displayName;

    // ✅ NOUVEAU : Méthodes utilitaires
    public boolean isSuperAdmin() {
        return this.globalRole == GlobalRole.SUPER_ADMIN;
    }

    @PrePersist
    public void prePersist() {
        if (this.role == null || this.role.isEmpty()) {
            this.role = "ROLE_USER";
        }
        if (this.globalRole == null) {
            this.globalRole = GlobalRole.USER;
        }
        if (this.theme == null || this.theme.isEmpty()) {
            this.theme = "light";
        }
        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = this.username;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.theme == null || this.theme.isEmpty()) {
            this.theme = "light";
        }
        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = this.username;
        }
    }
}