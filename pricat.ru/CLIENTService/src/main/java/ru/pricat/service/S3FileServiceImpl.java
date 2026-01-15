package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.pricat.config.properties.S3Properties;
import ru.pricat.exception.FileStorageException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

/**
 * Сервис для работы с файлами в S3 хранилище (MinIO).
 * Обеспечивает загрузку файлов в S3.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final S3Client s3Client;

    private final S3Properties s3Properties;

    @Override
    public void uploadFile(MultipartFile file, String filePath) {
        log.debug("Uploading file to S3: {}", filePath);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(filePath)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File successfully uploaded to S3: {}", filePath);
        } catch (S3Exception e) {
            log.error("S3 error while uploading file: {}", filePath, e);
            throw new FileStorageException("S3 storage error: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error while uploading file: {}", filePath, e);
            throw new FileStorageException("File read error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            s3Client.headObject(builder -> builder
                    .bucket(s3Properties.getBucketName())
                    .key(filePath)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new FileStorageException("S3 error while checking file existence: " + filePath, e);
        }
    }
}