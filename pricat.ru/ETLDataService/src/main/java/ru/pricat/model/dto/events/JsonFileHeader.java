package ru.pricat.model.dto.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Метаданные JSON файла с прайс-листом.
 * Record с поддержкой Builder паттерна.
 */
@Builder
@SuppressWarnings("unused")
public record JsonFileHeader(
        @NotBlank(message = "Company name is required")
        String company,

        @NotNull(message = "File processed timestamp is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant fileProcessedAt,

        String fileFormatVersion,
        Integer totalItems,
        String fileHash,
        UUID batchId
) {

    public JsonFileHeader {
        if (fileFormatVersion == null) {
            fileFormatVersion = "1.0";
        }
        if (batchId == null) {
            batchId = UUID.randomUUID();
        }
    }

    public static JsonFileHeader of(String company, Instant fileProcessedAt) {
        return JsonFileHeader.builder()
                .company(company)
                .fileProcessedAt(fileProcessedAt)
                .build();
    }

    public String getRoutingKey() {
        return "price." + company.toLowerCase();
    }

    public boolean hasItems() {
        return totalItems != null && totalItems > 0;
    }
}
