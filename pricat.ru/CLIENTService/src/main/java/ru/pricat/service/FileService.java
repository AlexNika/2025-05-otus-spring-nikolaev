package ru.pricat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.pricat.model.dto.response.FileResponseDto;

/**
 * Сервис для управления файлами прайс-листов.
 * Обеспечивает бизнес-логику загрузки и получения файлов.
 */
public interface FileService {

    /**
     * Загружает файл прайс-листа для указанного пользователя.
     *
     * @param username имя пользователя
     * @param file файл для загрузки
     * @param companyFolder папка компании в S3
     * @return DTO с информацией о загруженном файле
     */
    FileResponseDto uploadFile(String username, MultipartFile file, String companyFolder);

    /**
     * Получает страницу с файлами пользователя.
     *
     * @param username имя пользователя
     * @param pageable параметры пагинации
     * @return страница с DTO файлов
     */
    Page<FileResponseDto> getUserFiles(String username, Pageable pageable);

    /**
     * Проверяет, может ли пользователь загружать файлы.
     * Пользователь должен быть поставщиком (isSupplier) или иметь роль ADMIN.
     *
     * @param username имя пользователя
     * @return true если пользователь может загружать файлы, иначе false
     */
    boolean canUserUploadFiles(String username);
}