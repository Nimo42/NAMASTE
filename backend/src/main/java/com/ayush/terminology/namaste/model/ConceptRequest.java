package com.ayush.terminology.namaste.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "concept_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String term;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String suggestedCode;

    @Column(nullable = false)
    private String suggestedSystem;

    @Builder.Default
    private String reason = "";

    @Builder.Default
    private String requestedBy = "anonymous";

    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Builder.Default
    private String rejectionReason = "";

    @Builder.Default
    private String finalCode = "";

    private String processedBy;
    private LocalDateTime processedAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
