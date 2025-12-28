package com.example.AppNotiDo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "projects")
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du projet ne peut pas être vide")
    @Size(min = 1, max = 100, message = "Le nom doit faire entre 1 et 100 caractères")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String description;

    @Column(length = 7)
    private String color = "#3B82F6"; // Couleur hex par défaut (bleu)

    @Column(length = 50)
    private String icon = "folder"; // Icône par défaut

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public int getTaskCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public int getCompletedTaskCount() {
        if (tasks == null) return 0;
        return (int) tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();
    }

    public int getPendingTaskCount() {
        if (tasks == null) return 0;
        return (int) tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .count();
    }
}