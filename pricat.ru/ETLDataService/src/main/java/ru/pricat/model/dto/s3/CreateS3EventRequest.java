package ru.pricat.model.dto.s3;

import lombok.Builder;
import ru.pricat.model.entity.S3Event;
import ru.pricat.model.entity.S3EventTypes;

import java.time.Instant;

/**
 * DTO for create {@link S3Event}
 */
@Builder
public record CreateS3EventRequest(
        S3EventTypes eventType,
        String bucketName,
        String objectKey,
        Long objectSize,
        String objectETag,
        String objectContentType,
        Instant eventTime,
        String fullEventData) {
}
