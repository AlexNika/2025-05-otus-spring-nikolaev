package ru.pricat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Сущность для хранения информации о загруженных файлах прайс-листов.
 * Связана с клиентом через поле username.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "files", indexes = {
        @Index(name = "idx_files_username", columnList = "username"),
        @Index(name = "idx_files_upload_date", columnList = "upload_date DESC")
})
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Идентификатор пользователя, загрузившего файл.
     * Связь с таблицей clients через username.
     */
    @Column(name = "username", nullable = false, length = 64)
    private String username;

    /**
     * Оригинальное имя файла при загрузке.
     */
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    /**
     * Дата и время загрузки файла.
     */
    @Column(name = "upload_date", nullable = false)
    private OffsetDateTime uploadDate;

    /**
     * Путь к файлу в S3 хранилище.
     * Формат: {companyFolder}/{originalFileName}
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * Размер файла в байтах.
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * Поле для хранения даты создания клиента
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Поле для хранения даты последнего обновления клиента
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (uploadDate == null) {
            uploadDate = OffsetDateTime.now();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
