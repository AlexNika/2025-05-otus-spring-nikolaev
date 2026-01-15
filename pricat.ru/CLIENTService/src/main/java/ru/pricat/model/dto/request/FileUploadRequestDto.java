package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO для запроса загрузки файла.
 * Содержит файл для загрузки и целевую директорию.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record FileUploadRequestDto(
        MultipartFile file,
        String targetFolder
) {}
