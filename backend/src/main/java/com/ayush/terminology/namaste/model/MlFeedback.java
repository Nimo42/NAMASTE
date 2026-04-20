package com.ayush.terminology.namaste.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ml_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String query;

    private String predictedCode;

    @Builder.Default
    private String predictedSystem = "NAMASTE";

    @Builder.Default
    private String correctCode = "";

    @Builder.Default
    private String correctSystem = "";

    @Builder.Default
    private String feedbackType = "confirmed"; // "confirmed" or "corrected"

    private Double confidence;
    private String userId;

    @Builder.Default
    private Boolean reviewed = false;

    @Builder.Default
    private String adminDecision = "PENDING"; // PENDING, ACCEPTED, IGNORED

    private String reviewedBy;
    private LocalDateTime reviewedAt;

    @Builder.Default
    private String exportStatus = "DRAFT"; // DRAFT, EXPORTED

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
