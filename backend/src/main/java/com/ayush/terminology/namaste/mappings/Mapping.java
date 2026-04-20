package com.ayush.terminology.namaste.mappings;

import com.ayush.terminology.namaste.model.Concept;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_concept_id", nullable = false)
    private Concept fromConcept;

    @ManyToOne
    @JoinColumn(name = "to_concept_id", nullable = false)
    private Concept toConcept;

    @Column(name = "mapping_type")
    private String mappingType; // EQUIVALENT, NARROWER, BROADER

    private Double confidence;

    @Builder.Default
    private String status = "PENDING"; // PENDING, VERIFIED, REJECTED

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
