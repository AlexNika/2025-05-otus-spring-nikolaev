package ru.pricat.service.s3;

import software.amazon.awssdk.services.s3.model.Bucket;

import java.io.InputStream;
import java.util.List;

@SuppressWarnings("unused")
public interface S3StorageService {
    void createBucket();

    boolean isBucketExists();

    List<Bucket> listBuckets();

    List<String> listFiles(String companyFolder);

    InputStream downloadFile(String fileKey);

    void moveToProcessed(String sourceKey);

    String moveToUnprocessed(String sourceKey);

    void deleteFile(String fileKey);

    boolean ifFileExists(String fileKey);

    String calculateFileHash(String fileKey);
}
