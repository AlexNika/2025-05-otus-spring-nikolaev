package ru.pricat.service.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pricat.exception.FileProcessingException;
import ru.pricat.model.dto.events.PriceItemDto;
import ru.pricat.model.dto.history.FileProcessingResult;
import ru.pricat.model.entity.ProcessedFileHistory;
import ru.pricat.repository.ProcessedFileHistoryRepository;
import ru.pricat.service.messenger.RabbitMQBatchService;
import ru.pricat.service.parser.JsonStreamParserService;
import ru.pricat.service.s3.S3StorageService;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Оркестратор процесса обработки файлов прайс-листов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class FileProcessingOrchestratorImpl implements FileProcessingOrchestrator {

    private final S3StorageService s3StorageService;

    private final JsonStreamParserService jsonParserService;

    private final RabbitMQBatchService rabbitMQService;

    private final ProcessedFileHistoryRepository historyRepository;

    /**
     * Обрабатывает один файл прайс-листа.
     */
    @Transactional
    @Override
    public FileProcessingResult processFile(String company, String fileKey) {
        FileProcessingResult result = new FileProcessingResult(fileKey, company);
        String fileHash = null;
        try {
            fileHash = s3StorageService.calculateFileHash(fileKey);
            if (isAlreadyProcessed(fileKey, fileHash)) {
                result.setErrorMessage("File already processed");
                result.setSuccess(false);
                saveProcessingHistory(fileKey, fileHash, company,
                        ProcessedFileHistory.ProcessingStatus.DUPLICATE, 0, 0, null);
                return result;
            }
            if (!validateFileStructure(fileKey)) {
                result.setErrorMessage("Invalid file structure");
                result.setSuccess(false);
                saveProcessingHistory(fileKey, fileHash, company,
                        ProcessedFileHistory.ProcessingStatus.FAILED, 0, 0,
                        "Invalid JSON structure");
                return result;
            }
            result = processFileContent(fileKey, company, result);
            if (result.isSuccess()) {
                s3StorageService.moveToProcessed(fileKey);
                result.setS3Path(generateProcessedPath(fileKey));
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to process file {} for company {}: {}", fileKey, company, e.getMessage());
            result.setErrorMessage(e.getMessage());
            result.setSuccess(false);
            result.complete(false);
            saveProcessingHistory(fileKey, fileHash, company,
                    ProcessedFileHistory.ProcessingStatus.FAILED,
                    result.getItemsProcessed(), result.getItemsFailed(), e.getMessage());
            return result;
        }
    }

    /**
     * Проверяет, был ли файл уже обработан.
     */
    private boolean isAlreadyProcessed(String fileKey, String fileHash) {
        return historyRepository.existsByFilePathAndFileHash(fileKey, fileHash);
    }

    /**
     * Валидирует структуру JSON файла.
     */
    private boolean validateFileStructure(String fileKey) {
        try (InputStream stream = s3StorageService.downloadFile(fileKey)) {
            return jsonParserService.validateJsonStructure(stream);
        } catch (Exception e) {
            log.warn("File structure validation failed for {}: {}", fileKey, e.getMessage());
            return false;
        }
    }

    /**
     * Обрабатывает содержимое файла.
     */
    private FileProcessingResult processFileContent(String fileKey, String company,
                                                    FileProcessingResult result) {
        long startTime = System.currentTimeMillis();
        List<PriceItemDto> successfulItems = new ArrayList<>();
        List<String> failedItems = new ArrayList<>();
        UUID batchId = UUID.randomUUID();

        try (InputStream stream = s3StorageService.downloadFile(fileKey)) {
            jsonParserService.parseStream(stream,
                    item -> processSingleItem(item, company, successfulItems, failedItems, batchId),
                    failedItems::add
            );

            if (!successfulItems.isEmpty()) {
                try {
                    rabbitMQService.sendBatch(successfulItems, company, Instant.now(), batchId);
                } catch (Exception e) {
                    log.error("Failed to send batch to RabbitMQ for company {}: {}", company, e.getMessage());
                    failedItems.addAll(successfulItems.stream()
                            .map(item -> "RabbitMQ send failed: " + item.getProductId())
                            .toList());
                    successfulItems.clear();
                }
            }

            result.setItemsProcessed(successfulItems.size());
            result.setItemsFailed(failedItems.size());
            result.setSuccess(failedItems.isEmpty() || successfulItems.size() > failedItems.size());
            result.complete(true);
            ProcessedFileHistory.ProcessingStatus status = result.isSuccess() ?
                    ProcessedFileHistory.ProcessingStatus.SUCCESS :
                    ProcessedFileHistory.ProcessingStatus.PARTIAL;

            saveProcessingHistory(fileKey, s3StorageService.calculateFileHash(fileKey), company,
                    status, successfulItems.size(), failedItems.size(), null);

            log.info("File processing completed: {} successful, {} failed for {}",
                    successfulItems.size(), failedItems.size(), fileKey);
            return result;

        } catch (Exception e) {
            throw new FileProcessingException("Failed to process file content", e);
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("File processing completed - Company: {}, File: {}, Items: {}, Success: {}, Failed: {}, " +
                     "Time: {}ms", company, fileKey, successfulItems.size(), successfulItems.size(), failedItems.size(),
                    processingTime);
        }
    }

    /**
     * Обрабатывает один товар из файла.
     */
    private void processSingleItem(PriceItemDto item, String company,
                                   List<PriceItemDto> successfulItems,
                                   List<String> failedItems, UUID batchId) {
        try {
            if (item.isValid()) {
                successfulItems.add(item);
            } else {
                failedItems.add("Invalid item: " + item.getProductId());
            }
        } catch (Exception e) {
            failedItems.add("Processing error for " + item.getProductId() + ": " + e.getMessage());
        }
    }

    /**
     * Сохраняет историю обработки файла.
     */
    private void saveProcessingHistory(String filePath, String fileHash, String company,
                                       ProcessedFileHistory.ProcessingStatus status,
                                       int processedCount, int failedCount, String errorMessage) {
        try {
            ProcessedFileHistory history = ProcessedFileHistory.builder()
                    .fileName(extractFileName(filePath))
                    .filePath(filePath)
                    .fileHash(fileHash)
                    .company(company)
                    .processedAt(Instant.now())
                    .status(status)
                    .recordsProcessed(processedCount)
                    .recordsFailed(failedCount)
                    .errorMessage(errorMessage)
                    .batchId(UUID.randomUUID())
                    .processingTimeMs(System.currentTimeMillis())
                    .build();

            historyRepository.save(history);
            log.debug("Processing history saved for file: {}", filePath);

        } catch (Exception e) {
            log.error("Failed to save processing history for {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * Извлекает имя файла из полного пути.
     */
    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    /**
     * Генерирует путь к обработанному файлу.
     */
    private String generateProcessedPath(String originalKey) {
        String timestamp = Instant.now().toString().replace(":", "-");
        String filename = extractFileName(originalKey);
        return String.format("processed/%s_%s", timestamp, filename);
    }
}
