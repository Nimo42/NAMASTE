package com.ayush.terminology.namaste.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "system_id", nullable = false)
    private CodeSystem system;

    @Column(nullable = false)
    private String version;

    @Builder.Default
    private LocalDateTime importedAt = LocalDateTime.now();

    private Integer conceptCount;
    private String importedBy;
    private String notes;
}
