package ru.pricat.service;

import org.springframework.web.multipart.MultipartFile;
import ru.pricat.exception.FileStorageException;

public interface S3FileService {

    /**
     * Загружает файл в S3 хранилище.
     *
     * @param file файл для загрузки
     * @param filePath путь к файлу в S3 (включая имя файла)
     * @throws FileStorageException если произошла ошибка при загрузке
     */
    void uploadFile(MultipartFile file, String filePath);

    /**
     * Проверяет существование файла в S3.
     *
     * @param filePath путь к файлу в S3
     * @return true если файл существует, иначе false
     */
    boolean fileExists(String filePath);
}
