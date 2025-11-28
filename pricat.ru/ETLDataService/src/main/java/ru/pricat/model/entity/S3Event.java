package ru.pricat.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.pricat.model.BaseEntity;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;


/**
 * Сущность для сохранения событий, пришедших из S3 хранилища
 */
@Entity
@Table(name = "s3_events", schema = "etldataprocessor")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
@SuppressWarnings("SpellCheckingInspection")
public class S3Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    S3EventTypes eventType;

    /**
     * Имя бакета в котором произошло событие
     */
    @Column(name = "bucket_name", nullable = false)
    String bucketName;

    /**
     * Ключ объекта (путь к файлу в бакете)
     */
    @Column(name = "object_key", nullable = false)
    String objectKey;

    /**
     * Размер объекта в байтах (может быть null для событий удаления)
     */
    @Column(name = "object_size")
    Long objectSize;

    /**
     * ETag объекта (хеш содержимого)
     */
    @Column(name = "object_etag")
    String objectETag;

    /**
     * MIME-тип содержимого объекта
     */
    @Column(name = "object_content_type")
    String objectContentType;

    /**
     * Время возникновения события в S3
     */
    @Column(name = "event_time")
    Instant eventTime;

    /**
     * Полное событие в формате JSONB для хранения всей информации
     * Не передается в DTO для клиентских методов
     */
    @Column(name = "full_event_data")
    @JdbcTypeCode(SqlTypes.JSON)
    String fullEventData;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    @Builder.Default
    ProcessingStatus processingStatus = ProcessingStatus.RECEIVED;

    @Column(name = "processing_attempts")
    @Builder.Default
    Integer processingAttempts = 0;

    @Column(name = "last_error")
    String lastError;

    @Column(name = "processed_at")
    Instant processedAt;
}
