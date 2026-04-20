package com.ayush.terminology.namaste.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // doctor or system user ID
    private String patientId; // ABHA or demo ID
    private String action; // "SEARCH", "AI_SEARCH", "TRANSLATE", "BUNDLE_UPLOAD"
    private String endpoint; // "/codes/search"
    private LocalDateTime timestamp;
    private String payloadSummary; // what was searched/translated
}

