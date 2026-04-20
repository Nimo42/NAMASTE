package com.ayush.terminology.namaste.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private LocalDateTime importTime = LocalDateTime.now();

    @Column(nullable = false)
    private String codeSystem;

    @Column(nullable = false)
    private String fileName;

    @Builder.Default
    private String fileContentType = "text/csv";

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] fileContent;

    @Builder.Default
    private Integer rowCount = 0;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED

    @Builder.Default
    private String reportDescription = "";

    @Builder.Default
    private String importedBy = "unknown";

    @Builder.Default
    private String errorMessage = "";

    @Builder.Default
    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;
}
