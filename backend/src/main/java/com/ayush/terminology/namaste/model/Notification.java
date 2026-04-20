package com.ayush.terminology.namaste.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Builder.Default
    private String type = "SYSTEM_ALERT"; // DOCTOR_REGISTRATION, SYSTEM_ALERT

    @Builder.Default
    private String status = "UNREAD"; // UNREAD, READ, ARCHIVED

    // Metadata fields
    private String doctorId;
    private String doctorEmail;
    private String doctorNameString;
    private String hospital;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
