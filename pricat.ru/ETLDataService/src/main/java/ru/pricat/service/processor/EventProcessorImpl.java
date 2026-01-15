package ru.pricat.service.processor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.model.dto.events.JsonFileHeader;
import ru.pricat.model.dto.history.FileProcessingResult;
import ru.pricat.model.dto.s3.S3EventDto;
import ru.pricat.model.entity.Company;
import ru.pricat.model.entity.ProcessedFileHistory;
import ru.pricat.model.entity.ProcessingStatus;
import ru.pricat.repository.CompanyRepository;
import ru.pricat.repository.ProcessedFileHistoryRepository;
import ru.pricat.service.orchestrator.FileProcessingOrchestrator;
import ru.pricat.service.parser.JsonStreamParserService;
import ru.pricat.service.queue.EventQueueService;
import ru.pricat.service.s3.S3EventService;
import ru.pricat.service.s3.S3StorageService;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Обработчик событий из in-memory очереди
 * Работает в отдельном потоке для последовательной обработки
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class EventProcessorImpl implements EventProcessor {

    private final EventQueueService eventQueueService;

    private final S3EventService s3EventService;

    private final S3StorageService s3StorageService;

    private final CompanyRepository companyRepository;

    private final JsonStreamParserService jsonParserService;

    private final FileProcessingOrchestrator fileProcessingOrchestrator;

    private final ProcessedFileHistoryRepository historyRepository;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    volatile boolean running = false;

    @Override
    @PostConstruct
    public void start() {
        log.info("Starting EventProcessor...");
        running = true;
        new Thread(this::processEventsSequentially, "EventProcessor-Thread").start();
        log.info("EventProcessor started successfully");
    }

    @Override
    public void stop() {
        log.info("Stopping EventProcessor...");
        running = false;
        log.info("EventProcessor stopped");
    }

    /**
     * Упрощенная последовательная обработка событий
     */
    private void processEventsSequentially() {
        log.info("Event processing started in sequential mode");
        while (true) {
            try {
                S3EventDto eventDto = eventQueueService.takeEvent();
                log.info("Processing event from queue: {} (key: {})",
                        eventDto.id(), eventDto.objectKey());
                processSingleEvent(eventDto);
            } catch (InterruptedException e) {
                log.info("Event processor interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Unexpected error in event processor", e);
            }
        }
        log.info("Event processing finished");
    }

    private void processSingleEvent(S3EventDto eventDto) {
        log.info("Processing event: {} (type: {}, bucket: {}, key: {})",
                eventDto.id(), eventDto.eventType(), eventDto.bucketName(), eventDto.objectKey());
        try {
            s3EventService.updateEventStatus(eventDto.id(), ProcessingStatus.PROCESSING, null);
            handleS3Event(eventDto);
            s3EventService.updateEventStatus(eventDto.id(), ProcessingStatus.COMPLETED, null);
            eventQueueService.removeEventSignature(eventDto);
            log.info("Event processed successfully: {}", eventDto.id());
        } catch (Exception e) {
            log.error("Error processing event: {}", eventDto.id(), e);
            s3EventService.updateEventStatus(eventDto.id(), ProcessingStatus.FAILED, e.getMessage());
            eventQueueService.removeEventSignature(eventDto);
        }
    }

    private void handleS3Event(S3EventDto eventDto) {
        String objectKey = eventDto.objectKey();
        log.debug("objectKey: {}", objectKey);
        switch (eventDto.eventType()) {
            case S3_OBJECTCREATED_PUT:
            case S3_OBJECTCREATED_COMPLETEMULTIPARTUPLOAD:
                handleFileCreated(eventDto);
                break;
            case S3_OBJECTREMOVED_DELETE:
                handleFileDeleted(eventDto);
                break;
            default:
                log.warn("Unhandled event type: {}, only logging", eventDto.eventType());
                log.info("S3 event logged - Type: {}, Bucket: {}, Key: {}",
                        eventDto.eventType(), eventDto.bucketName(), eventDto.objectKey());
        }
    }

    private void handleFileCreated(S3EventDto eventDto) {
        String objectKey = eventDto.objectKey();
        log.info("Processing file creation: {}", objectKey);
        try {
            String companyFolder = extractCompanyFolder(objectKey).toLowerCase();
            log.debug("Extracted company folder: {} from key: {}", companyFolder, objectKey);
            Optional<Company> companyOpt = companyRepository.findByCompanyFolderIgnoreCase(companyFolder);
            if (companyOpt.isEmpty()) {
                handleCompanyMismatch(objectKey, companyFolder, null, "Company not found");
                return;
            }
            Company company = companyOpt.get();
            log.debug("Found by CompanyFolder company: {}", company.getCompanyFolder());
            if (!company.getIsActive()) {
                handleCompanyMismatch(objectKey, companyFolder, company.getCompanyNameEN(), "Company is inactive");
                return;
            }
            try (InputStream fileStream = s3StorageService.downloadFile(objectKey)) {
                JsonFileHeader metadata = jsonParserService.extractMetadata(fileStream);
                String jsonCompany = metadata.company().toLowerCase();

                if (!companyFolder.equals(jsonCompany)) {
                    handleCompanyMismatch(objectKey, companyFolder, jsonCompany,
                            String.format("Company mismatch: folder '%s' vs JSON '%s'", companyFolder, jsonCompany));
                    return;
                }
            }
            FileProcessingResult result = fileProcessingOrchestrator.processFile(companyFolder, objectKey);
            if (result.isSuccess()) {
                log.info("File processing completed successfully: {} items processed, {} failed",
                        result.getItemsProcessed(), result.getItemsFailed());
            } else {
                log.warn("File processing completed with issues: {} items processed, {} failed. Error: {}",
                        result.getItemsProcessed(), result.getItemsFailed(), result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Failed to process file creation for {}: {}", objectKey, e.getMessage(), e);
            throw new RuntimeException("File processing failed", e);
        }
    }

    /**
     * Для события удаления, производим только логирование
     */
    private void handleFileDeleted(S3EventDto eventDto) {
        log.info("File deletion event - Bucket: {}, Key: {}",
                eventDto.bucketName(), eventDto.objectKey());
    }

    /**
     * Извлекает companyFolder из S3 ключа
     * Пример: "company_a/company_a_pricelist.json" -> "company_a"
     */
    private String extractCompanyFolder(String objectKey) {
        if (objectKey == null || !objectKey.contains("/")) {
            throw new IllegalArgumentException("Invalid object key format: " + objectKey);
        }
        return objectKey.substring(0, objectKey.indexOf("/"));
    }

    /**
     * Обрабатывает несоответствие компании
     */
    private void handleCompanyMismatch(String objectKey, String companyFolder, String jsonCompany, String reason) {
        log.warn("Company validation failed for {}: {}", objectKey, reason);
        String unprocessedKey = s3StorageService.moveToUnprocessed(objectKey);
        String fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);
        String fileHash = s3StorageService.calculateFileHash(unprocessedKey);
        ProcessedFileHistory history = ProcessedFileHistory.createFailure(
                fileName, objectKey, fileHash, companyFolder, reason
        );
        historyRepository.save(history);
        log.info("File moved to unprocessed due to company mismatch: {}", objectKey);
    }
}
