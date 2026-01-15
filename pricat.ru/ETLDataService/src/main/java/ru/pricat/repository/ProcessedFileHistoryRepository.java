package ru.pricat.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.pricat.model.entity.ProcessedFileHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с историей обработки файлов.
 * Обеспечивает проверку идемпотентности и поиск обработанных файлов.
 */
@Repository
@SuppressWarnings("unused")
@RepositoryRestResource(path = "processed_file_history", collectionResourceRel = "processed_file_history")
public interface ProcessedFileHistoryRepository extends JpaRepository<ProcessedFileHistory, Long> {

    /**
     * Проверяет, был ли файл уже обработан по его пути и хэшу.
     * Используется для предотвращения повторной обработки.
     */
    Optional<ProcessedFileHistory> findByFilePathAndFileHash(String filePath, String fileHash);

    /**
     * Находит историю обработки по correlationId для сквозной трассировки.
     */
    Optional<ProcessedFileHistory> findByCorrelationId(String correlationId);

    /**
     * Находит историю обработки по batchId (идентификатору партии товаров).
     */
    Optional<ProcessedFileHistory> findByBatchId(UUID batchId);

    /**
     * Ищет файлы по компании и статусу обработки.
     */
    List<ProcessedFileHistory> findByCompanyAndStatus(String company, ProcessedFileHistory.ProcessingStatus status);

    /**
     * Ищет файлы, обработанные после указанной даты.
     */
    List<ProcessedFileHistory> findByProcessedAtAfter(Instant processedAfter);

    /**
     * Ищет файлы, обработанные в указанный период.
     */
    List<ProcessedFileHistory> findByProcessedAtBetween(Instant start, Instant end);

    /**
     * Проверяет существование файла с таким путем и хэшем.
     */
    boolean existsByFilePathAndFileHash(String filePath, String fileHash);

    /**
     * Находит последние N обработанных файлов для компании.
     */
    List<ProcessedFileHistory> findTop10ByCompanyOrderByProcessedAtDesc(String company);

    /**
     * Кастомный запрос для получения статистики по компании.
     */
    @Query("""
                SELECT new map(
                    COUNT(pfh) as totalFiles,
                    SUM(pfh.recordsProcessed) as totalRecords,
                    AVG(pfh.processingTimeMs) as avgProcessingTime,
                    SUM(CASE WHEN pfh.status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount,
                    SUM(CASE WHEN pfh.status = 'FAILED' THEN 1 ELSE 0 END) as failedCount
                )
                FROM ProcessedFileHistory pfh
                WHERE pfh.company = :company
                AND pfh.processedAt >= :since
            """)
    Object getProcessingStatistics(@Param("company") String company, @Param("since") Instant since);

    /**
     * Кастомный запрос для поиска дубликатов файлов.
     */
    @Query("""
                SELECT pfh
                FROM ProcessedFileHistory pfh
                WHERE pfh.fileName = :fileName
                AND pfh.fileSize = :fileSize
                AND pfh.processedAt >= :since
                ORDER BY pfh.processedAt DESC
            """)
    List<ProcessedFileHistory> findPotentialDuplicates(
            @Param("fileName") String fileName,
            @Param("fileSize") Long fileSize,
            @Param("since") Instant since
    );

    /**
     * Кастомный запрос для очистки старых записей (для housekeeping).
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProcessedFileHistory pfh WHERE pfh.processedAt < :cutoffDate")
    int deleteOldRecords(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Находит файлы с ошибками обработки для повторной попытки.
     */
    List<ProcessedFileHistory> findByStatusAndProcessedAtBefore(
            ProcessedFileHistory.ProcessingStatus status,
            Instant processedBefore
    );
}

