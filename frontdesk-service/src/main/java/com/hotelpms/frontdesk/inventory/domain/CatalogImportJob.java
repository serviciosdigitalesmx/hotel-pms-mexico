package com.hotelpms.frontdesk.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** Tenant-scoped human-reviewed catalog ingestion job. */
@Entity
@Table(name = "catalog_import_jobs")
@Getter
@Setter
@NoArgsConstructor
public class CatalogImportJob {
    public enum Status {
        INGESTED,
        EXTRACTED,
        REVIEW_REQUIRED,
        APPLIED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "raw_text", nullable = false, columnDefinition = "text")
    private String rawText;

    @Column(name = "extracted_rows", nullable = false, columnDefinition = "text")
    private String extractedRows;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status = Status.INGESTED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
