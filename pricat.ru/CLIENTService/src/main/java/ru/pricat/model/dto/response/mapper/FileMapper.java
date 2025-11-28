package ru.pricat.model.dto.response.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.pricat.model.File;
import ru.pricat.model.dto.response.FileResponseDto;

/**
 * Маппер для преобразования сущности File в FileResponseDto.
 * Использует MapStruct для автоматического генерации кода.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper {

    /**
     * Преобразует сущность File в FileResponseDto.
     * Форматирует размер файла в читаемый вид.
     *
     * @param file сущность File
     * @return FileResponseDto
     */
    @Mapping(target = "formattedFileSize", expression = "java(formatFileSize(file.getFileSize()))")
    FileResponseDto toFileResponseDto(File file);

    /**
     * Форматирует размер файла в байтах в читаемую строку.
     *
     * @param size размер файла в байтах
     * @return форматированная строка размера файла
     */
    default String formatFileSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }
}
