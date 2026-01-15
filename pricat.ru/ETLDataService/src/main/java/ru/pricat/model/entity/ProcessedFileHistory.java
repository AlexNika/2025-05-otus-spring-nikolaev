package ru.pricat.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.pricat.model.BaseEntity;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * Сущность для хранения истории обработки файлов.
 */
@Entity
@Table(name = "processed_file_history", schema = "etldataprocessor",
        indexes = {
                @Index(name = "idx_file_path_hash", columnList = "filePath, fileHash"),
                @Index(name = "idx_processed_at", columnList = "processedAt"),
                @Index(name = "idx_company_status", columnList = "company, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_file_processing", columnNames = {"filePath", "fileHash"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("unused")
@FieldDefaults(level = PRIVATE)
public class ProcessedFileHistory extends BaseEntity {

    public enum ProcessingStatus {
        SUCCESS, FAILED, PARTIAL, DUPLICATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "file_name", nullable = false, length = 500)
    String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    String filePath;

    @Column(name = "file_hash", nullable = false, length = 64)
    String fileHash;

    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "company", nullable = false, length = 100)
    String company;

    @Column(name = "processed_at", nullable = false)
    Instant processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    ProcessingStatus status;

    @Column(name = "records_processed")
    @Builder.Default
    Integer recordsProcessed = 0;

    @Column(name = "records_failed")
    @Builder.Default
    Integer recordsFailed = 0;

    @Column(name = "error_message", length = 2000)
    String errorMessage;

    @Column(name = "batch_id", length = 36)
    UUID batchId;

    @Column(name = "processing_time_ms")
    Long processingTimeMs;

    @Column(name = "s3_etag", length = 100)
    String s3ETag;

    /**
     * Общее количество записей в файле.
     */
    public Integer getTotalRecords() {
        return (recordsProcessed != null ? recordsProcessed : 0) +
               (recordsFailed != null ? recordsFailed : 0);
    }

    /**
     * Процент успешно обработанных записей.
     */
    public Double getSuccessRate() {
        if (getTotalRecords() == 0) return 0.0;
        return (recordsProcessed != null ? recordsProcessed.doubleValue() : 0.0) / getTotalRecords() * 100;
    }

    /**
     * Factory method для удобного создания.
     */
    public static ProcessedFileHistory createSuccess(String fileName, String filePath,
                                                     String fileHash, String company,
                                                     int recordsProcessed) {
        return ProcessedFileHistory.builder()
                .fileName(fileName)
                .filePath(filePath)
                .fileHash(fileHash)
                .company(company)
                .processedAt(Instant.now())
                .status(ProcessingStatus.SUCCESS)
                .recordsProcessed(recordsProcessed)
                .recordsFailed(0)
                .batchId(UUID.randomUUID())
                .build();
    }

    /**
     * Factory method для неудачной обработки.
     */
    public static ProcessedFileHistory createFailure(String fileName, String filePath,
                                                     String fileHash, String company,
                                                     String errorMessage) {
        return ProcessedFileHistory.builder()
                .fileName(fileName)
                .filePath(filePath)
                .fileHash(fileHash)
                .company(company)
                .processedAt(Instant.now())
                .status(ProcessingStatus.FAILED)
                .errorMessage(errorMessage)
                .batchId(UUID.randomUUID())
                .build();
    }
}

