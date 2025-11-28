package ru.pricat.indicators;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.pricat.config.S3Properties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Кастомный Health индикатор для S3Minio
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class S3HealthIndicator implements HealthIndicator {

    final S3Client s3Client;

    final String bucketName;

    public S3HealthIndicator(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.bucketName = s3Properties.getBucketName();
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        try {
            ListBucketsResponse bucketsResponse = s3Client.listBuckets();
            details.put("bucketsCount", bucketsResponse.buckets().size());
            details.put("connection", "ESTABLISHED");
            if (bucketsResponse.owner() != null) {
                details.put("owner", bucketsResponse.owner().displayName());
            }
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            details.put("bucket", "AVAILABLE");
            details.put("bucketName", bucketName);
            String testKey = "health-check-" + Instant.now().toEpochMilli();
            String testContent = "health-check-content";
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(testKey)
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromString(testContent));
            details.put("writePermissions", "GRANTED");
            details.put("testFile", testKey);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(testKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            details.put("deletePermissions", "GRANTED");
            log.debug("S3 MinIO health check passed for bucket: {}", bucketName);
            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (S3Exception e) {
            log.warn("S3 MinIO health check failed: {}", e.getMessage());
            details.put("errorCode", e.awsErrorDetails().errorCode());
            details.put("errorMessage", e.awsErrorDetails().errorMessage());
            return Health.down()
                    .withDetails(details)
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during S3 MinIO health check: {}", e.getMessage());
            details.put("error", e.getMessage());
            details.put("errorType", e.getClass().getSimpleName());
            return Health.outOfService()
                    .withDetails(details)
                    .build();
        }
    }
}