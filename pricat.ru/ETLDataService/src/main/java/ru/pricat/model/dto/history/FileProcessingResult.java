package ru.pricat.model.dto.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Результат обработки файла.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class FileProcessingResult {

    @Builder.Default
    private UUID processingId = UUID.randomUUID();

    private String fileName;
    private String company;

    @Builder.Default
    private Instant startedAt = Instant.now();

    private Instant completedAt;
    private boolean success;
    private int itemsProcessed;
    private int itemsFailed;
    private String errorMessage;
    private String s3Path;

    public FileProcessingResult(String fileName, String company) {
        this.fileName = fileName;
        this.company = company;
        this.startedAt = Instant.now();
        this.processingId = UUID.randomUUID();
    }

    /**
     * Общее количество обработанных элементов.
     */
    public int getTotalItems() {
        return itemsProcessed + itemsFailed;
    }

    /**
     * Завершает обработку с указанием результата.
     */
    public void complete(boolean success) {
        this.success = success;
        this.completedAt = Instant.now();
    }

    /**
     * Factory method для успешной обработки.
     */
    public static FileProcessingResult success(String fileName, String company, int itemsProcessed) {
        FileProcessingResult result = new FileProcessingResult(fileName, company);
        result.setSuccess(true);
        result.setItemsProcessed(itemsProcessed);
        result.complete(true);
        return result;
    }

    /**
     * Factory method для неудачной обработки.
     */
    public static FileProcessingResult failure(String fileName, String company, String errorMessage) {
        FileProcessingResult result = new FileProcessingResult(fileName, company);
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.complete(false);
        return result;
    }
}
