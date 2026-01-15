package ru.pricat.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Конфигурация для настроек файлов.
 * Настройки загружаются из application.yml с префиксом app.file.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileProperties {

    /**
     * Максимальный размер файла в байтах.
     */
    private long maxFileSize = 20 * 1024 * 1024;

    /**
     * Разрешенные типы файлов (расширения).
     */
    private String[] allowedExtensions = {".xlsx", ".xls", ".csv", ".json", ".xml", ".txt"};
}