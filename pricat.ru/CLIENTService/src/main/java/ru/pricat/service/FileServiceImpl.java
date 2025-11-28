package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;
import ru.pricat.config.properties.FileProperties;
import ru.pricat.exception.FileValidationException;
import ru.pricat.model.Client;
import ru.pricat.model.File;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.model.dto.response.FileResponseDto;
import ru.pricat.model.dto.response.mapper.FileMapper;
import ru.pricat.repository.FileRepository;

import java.util.Locale;

/**
 * Реализация сервиса для управления файлами прайс-листов.
 * Обеспечивает валидацию, загрузку в S3 и сохранение метаданных в БД.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    private final ClientService clientService;

    private final S3FileService s3FileService;

    private final FileProperties fileProperties;

    private final FileMapper fileMapper;

    @Override
    @Transactional
    public FileResponseDto uploadFile(String username, MultipartFile file, String companyFolder) {
        log.info("Uploading file for user: {}, original filename: {}", username, file.getOriginalFilename());
        validateFile(file);
        String filePath = prepareFilePath(companyFolder, file.getOriginalFilename());
        s3FileService.uploadFile(file, filePath);
        File fileEntity = saveFileMetadata(username, file, filePath);
        log.info("File successfully uploaded for user: {}, file id: {}", username, fileEntity.getId());
        return fileMapper.toFileResponseDto(fileEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileResponseDto> getUserFiles(String username, Pageable pageable) {
        log.debug("Getting files for user: {}, page: {}, size: {}", username, pageable.getPageNumber(),
                pageable.getPageSize());
        return fileRepository.findByUsernameOrderByUploadDateDesc(username, pageable)
                .map(fileMapper::toFileResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserUploadFiles(String username) {
        AdminProfileDto currentUserProfile = clientService.getCurrentUserProfile(username);
        boolean canUpload = currentUserProfile.isSupplier() || currentUserProfile.roles().contains(String.valueOf(Client.Role.ADMIN));
        log.debug("Checking if user: {} can upload files. Can upload files: {}", username, canUpload);
        return canUpload;
    }

    /**
     * Валидирует загружаемый файл.
     *
     * @param file файл для валидации
     * @throws FileValidationException если файл не прошел валидацию
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        if (file.getSize() > fileProperties.getMaxFileSize()) {
            throw new FileValidationException("File size exceeds maximum allowed size of " +
                                              fileProperties.getMaxFileSize() + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new FileValidationException("File name is empty");
        }

        String extension = getFileExtension(originalFilename).toLowerCase(Locale.ROOT);
        boolean allowed = false;
        for (String allowedExt : fileProperties.getAllowedExtensions()) {
            if (allowedExt.toLowerCase(Locale.ROOT).equals(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new FileValidationException("File type not allowed. Allowed types: " +
                                              String.join(", ", fileProperties.getAllowedExtensions()));
        }
    }

    /**
     * Извлекает расширение файла из имени.
     *
     * @param filename имя файла
     * @return расширение файла (включая точку)
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * Подготавливает путь для сохранения файла в S3.
     * Формат: {companyFolder}/{originalFileName}
     *
     * @param companyFolder папка компании
     * @param originalFilename оригинальное имя файла
     * @return путь к файлу в S3
     */
    private String prepareFilePath(String companyFolder, String originalFilename) {
        log.debug("Preparing file path for company: {}, original filename: {}", companyFolder, originalFilename);
        String filePath = HtmlUtils.htmlEscape(companyFolder) + "/" + HtmlUtils.htmlEscape(originalFilename);
        log.debug("File path to upload: {}", filePath);
        return filePath;
    }

    /**
     * Сохраняет метаданные файла в БД.
     *
     * @param username имя пользователя
     * @param file загруженный файл
     * @param filePath путь к файлу в S3
     * @return сохраненная сущность File
     */
    private File saveFileMetadata(String username, MultipartFile file, String filePath) {
        File fileEntity = File.builder()
                .username(username)
                .originalFileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .build();

        return fileRepository.save(fileEntity);
    }
}