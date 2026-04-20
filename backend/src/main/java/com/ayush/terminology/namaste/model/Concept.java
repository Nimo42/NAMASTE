package com.ayush.terminology.namaste.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "concepts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code", "system_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Concept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String description;

    @ManyToOne
    @JoinColumn(name = "system_id", nullable = false)
    private CodeSystem codeSystem;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "lifecycle_status")
    private String lifecycleStatus = "ACTIVE"; // ACTIVE, INACTIVE, ARCHIVED

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
