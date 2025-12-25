package com.example.AppNotiDo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "kanban_config")
public class KanbanConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    // Colonnes status visibles (format: "TODO,IN_PROGRESS,DONE")
    @Column(name = "visible_status_columns", nullable = false)
    private String visibleStatusColumns = "TODO,IN_PROGRESS,DONE";

    // Colonnes tags actives (format: "work,urgent,personal")
    @Column(name = "active_tag_columns", length = 500)
    private String activeTagColumns = "";

    // Ordre des colonnes (format: "TODO,IN_PROGRESS,DONE,work,urgent")
    @Column(name = "columns_order", length = 500)
    private String columnsOrder = "TODO,IN_PROGRESS,DONE";

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.visibleStatusColumns == null || this.visibleStatusColumns.isEmpty()) {
            this.visibleStatusColumns = "TODO,IN_PROGRESS,DONE";
        }
        if (this.activeTagColumns == null) {
            this.activeTagColumns = "";
        }
        if (this.columnsOrder == null || this.columnsOrder.isEmpty()) {
            this.columnsOrder = "TODO,IN_PROGRESS,DONE";
        }
    }
}