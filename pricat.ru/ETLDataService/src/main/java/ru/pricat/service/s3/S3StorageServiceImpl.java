package ru.pricat.service.s3;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.config.S3Properties;
import ru.pricat.exception.StorageException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3StorageServiceImpl implements S3StorageService {

    final S3Client s3Client;

    final String bucketName;

    public S3StorageServiceImpl(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.bucketName = s3Properties.getBucketName();
    }

    @Override
    public void createBucket() {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createBucketRequest);
        } catch (S3Exception e) {
            log.error("Failed to create bucket {}: {}", bucketName, e.getMessage());
            throw new StorageException("Failed to create bucket", e);
        }
    }

    /**
     * Метод проверяет, существует ли корзина S3 хранилища или нет.
     */
    @Override
    public boolean isBucketExists() {
        try {
            List<String> bucketsName = this.listBuckets().stream().map(Bucket::name).toList();
            return bucketsName.contains(bucketName);
        } catch (S3Exception e) {
            log.error("Failed to check if bucket exists {}: {}", bucketName, e.getMessage());
            throw new StorageException("Failed to check if bucket exists", e);
        }
    }

    /**
     * Метод возвращает список корзин S3 хранилища.
     */
    @Override
    public List<Bucket> listBuckets() {
        List<Bucket> allBuckets = new ArrayList<>();
        String nextToken = null;
        do {
            String continuationToken = nextToken;
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets(
                    request -> request.continuationToken(continuationToken)
            );

            allBuckets.addAll(listBucketsResponse.buckets());
            nextToken = listBucketsResponse.continuationToken();
        } while (nextToken != null);
        return allBuckets;
    }

    /**
     * Метод возвращает список файлов для указанной корзины и компании.
     */
    @Override
    public List<String> listFiles(String companyFolder) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(MessageFormat.format("{0}/", companyFolder))
                    .build();
            log.info("-> request (bucket): {}", request.bucket());
            log.info("-> request (prefix): {}", request.prefix());
            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> !key.endsWith("/"))
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("Failed to list files for company {}: {}", companyFolder, e.getMessage());
            throw new StorageException("Failed to list files in S3", e);
        }
    }

    /**
     * Скачивает файл из S3 хранилища как InputStream.
     */
    @Override
    public InputStream downloadFile(String fileKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            return s3Client.getObject(request);

        } catch (S3Exception e) {
            log.error("Failed to download file {}: {}", fileKey, e.getMessage());
            throw new StorageException("Failed to download file from S3", e);
        }
    }

    /**
     * Перемещает файл в папку processed.
     */
    @Override
    public void moveToProcessed(String sourceKey) {
        log.info("-> S3StorageService -> moveToProcessed method: {}", sourceKey);
        try {
            String destinationKey = generateProcessedKey(sourceKey);
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationKey)
                    .build();
            s3Client.copyObject(copyRequest);
            this.deleteFile(sourceKey);
            log.info("File moved to processed: {} -> {}", sourceKey, destinationKey);
        } catch (S3Exception e) {
            log.error("Failed to move file {} to processed: {}", sourceKey, e.getMessage());
            throw new StorageException("Failed to move file to processed folder", e);
        }
    }

    @Override
    public String moveToUnprocessed(String sourceKey) {
        log.info("-> S3StorageService -> moveToUnprocessed method: {}", sourceKey);
        String destinationKey;
        try {
            destinationKey = generateUnprocessedKey(sourceKey);
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationKey)
                    .build();
            s3Client.copyObject(copyRequest);
            this.deleteFile(sourceKey);
            log.info("File moved to unprocessed: {} -> {}", sourceKey, destinationKey);
            return destinationKey;
        } catch (S3Exception e) {
            log.error("Failed to move file {} to unprocessed: {}", sourceKey, e.getMessage());
            throw new StorageException("Failed to move file to unprocessed folder", e);
        }
    }

    /**
     * Удаляет файл из S3.
     */
    @Override
    public void deleteFile(String fileKey) {
        log.info("-> S3StorageService -> deleteFile method: {}", fileKey);
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            log.error("Failed to delete file {}: {}", fileKey, e.getMessage());
            throw new StorageException("Failed to delete file from S3", e);
        }
    }

    /**
     * Проверяет существование файла.
     */
    @Override
    public boolean ifFileExists(String fileKey) {
        log.info("-> S3StorageService -> ifFileExists method: {}", fileKey);
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Failed to check file existence {}: {}", fileKey, e.getMessage());
            throw new StorageException("Failed to check file existence in S3", e);
        }
    }

    /**
     * Генерирует хэш файла для проверки идемпотентности.
     */
    @Override
    public String calculateFileHash(String fileKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            HeadObjectResponse response = s3Client.headObject(request);
            return response.eTag();
        } catch (S3Exception e) {
            log.error("Failed to calculate hash for file {}: {}", fileKey, e.getMessage());
            throw new StorageException("Failed to calculate file hash", e);
        }
    }

    /**
     * Генерирует ключ для папки processed.
     */
    @NonNull
    private String generateProcessedKey(@NonNull String originalKey) {
        String timestamp = Instant.now().toString().replace(":", "-");
        String filename = originalKey.substring(originalKey.lastIndexOf("/") + 1);
        String company = originalKey.substring(0, originalKey.lastIndexOf("/"));
        return String.format("processed/%s/%s_%s", company, timestamp, filename);
    }

    /**
     * Генерирует ключ для папки unprocessed.
     */
    @NonNull
    private String generateUnprocessedKey(@NonNull String originalKey) {
        String timestamp = Instant.now().toString().replace(":", "-");
        String filename = originalKey.substring(originalKey.lastIndexOf("/") + 1);
        return String.format("unprocessed/%s_%s", timestamp, filename);
    }

}
