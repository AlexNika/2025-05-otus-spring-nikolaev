package ru.pricat.model.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией о загруженном файле.
 * Содержит метаданные файла для отображения в UI.
 */
@Builder
public record FileResponseDto(
        UUID id,
        String username,
        String originalFileName,
        OffsetDateTime uploadDate,
        String filePath,
        Long fileSize,
        String formattedFileSize
) {}