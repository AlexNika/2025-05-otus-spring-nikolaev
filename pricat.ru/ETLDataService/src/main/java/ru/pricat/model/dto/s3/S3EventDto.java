package ru.pricat.model.dto.s3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import ru.pricat.model.entity.S3Event;
import ru.pricat.model.entity.S3EventTypes;

import java.time.Instant;

/**
 * DTO for {@link S3Event}
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record S3EventDto(String correlationId,
                         Long id,
                         S3EventTypes eventType,
                         String bucketName,
                         String objectKey,
                         Long objectSize,
                         String objectETag,
                         String objectContentType,
                         Instant eventTime) {
}